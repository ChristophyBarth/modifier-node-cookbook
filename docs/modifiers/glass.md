# Modifier.glass (+ glassSource)

> Backdrop sampler that reads as glass. Defaults render as **clear glass**: blur + specular border + diagonal sheen, barely-there scrim. Opt into **frosted glass** by raising `saturation`, `tint` alpha, `sheenAlpha`, and `borderAlpha`. Opt into the **rainbow rim** by passing `chromaticAberration > 0.dp`.

## When to use

- Translucent panels, captions, controls, bottom sheets that need to feel like a physical pane laid over imagery.
- Anywhere you want the underlying content to read through with a mild distortion (clear) or with a luminous frost (frosted).
- Pairs naturally with `HorizontalPager`, scrolling carousels, video previews, animated gradients: anything where the imagery moves and the panel should track it.

## When _not_ to use

- The backdrop is opaque, static, and pre-known. A flat `background()` is cheaper.
- API 21–30 is the dominant target. The blur and saturation chain both no-op below API 31; the panel still draws (tint + sheen + border) but doesn't read as glass.

## Usage

### Source (always the same)

```kotlin
val state = rememberGlassState()

HorizontalPager(
    state = pagerState,
    modifier = Modifier.fillMaxSize().glassSource(state),  // records the imagery
) { i -> AsyncImage(model = pages[i], contentDescription = null, …) }
```

### Clear glass (default)

Zero config. Blur + specular border + sheen. Barely-there scrim so the backdrop reads through clearly.

```kotlin
Modifier
    .clip(RoundedCornerShape(14.dp))
    .glass(state)
    .padding(horizontal = 16.dp, vertical = 12.dp)
```

### Clear glass with rainbow rim (opt in)

Pass `chromaticAberration` to add the prism fringe. `hueShift` rotates the spectrum (0f–1f = 0°–360°).

```kotlin
Modifier
    .clip(RoundedCornerShape(14.dp))
    .glass(
        state = state,
        chromaticAberration = 24.dp,   // rainbow rim width/intensity (API 33+)
        hueShift = 0f,                 // rotate spectrum; 0.5f = 180° opposite colours
    )
```

### Frosted glass (opt in)

Raise saturation, tint, sheen, and border for the full glassmorphism treatment.

```kotlin
val shape = RoundedCornerShape(20.dp)

Modifier
    .clip(shape)
    .glass(
        state = state,
        blurRadius = 24.dp,
        saturation = 1.6f,                        // luminous, not muddy (API 33+)
        tint = Color.White.copy(alpha = 0.15f),   // light frost
        sheenAlpha = 0.18f,                       // diagonal highlight
        borderAlpha = 0.45f,                      // 1-px specular stroke
        shape = shape,
    )
```

### Common configurations

| Want | How |
|---|---|
| Clear glass (default) | `Modifier.glass(state)` |
| Clear + rainbow prism | `Modifier.glass(state, chromaticAberration = 24.dp)` |
| Rainbow with rotated spectrum | `Modifier.glass(state, chromaticAberration = 24.dp, hueShift = 0.33f)` |
| Frosted | `Modifier.glass(state, blurRadius = 24.dp, saturation = 1.6f, tint = Color.White.copy(alpha = 0.15f), sheenAlpha = 0.18f, borderAlpha = 0.45f)` |
| Pure backdrop blur, no scrim | `Modifier.glass(state, tint = Color.Transparent, sheenAlpha = 0f, borderAlpha = 0f)` |
| Heavy iOS-style frost | `Modifier.glass(state, blurRadius = 40.dp, saturation = 1.8f, tint = Color.White.copy(alpha = 0.30f), sheenAlpha = 0.30f, borderAlpha = 0.55f)` |

## Parameters

### `rememberGlassState()`

Returns a `GlassState` tied to the current composition's graphics-layer pool. One state per source/panel pair.

### `Modifier.glassSource(state)`

Records the receiver into `state.layer` on every draw and plays it back unblurred so the source still appears normally. The recording is what the panel reads.

### `Modifier.glass(state, blurRadius, saturation, chromaticAberration, hueShift, tint, sheenAlpha, borderAlpha, shape)`

| Name | Type | Default | Notes |
|---|---|---|---|
| `state` | `GlassState` | (required) | Same instance passed to `glassSource`. |
| `blurRadius` | `Dp` | `12.dp` | Blur strength on API 31+. |
| `saturation` | `Float` | `1.15f` | Chromaticity multiplier on the blurred backdrop (API 33+). `1f` = no change; `1.4f`–`1.8f` for luminous frost. |
| `chromaticAberration` | `Dp` | `0.dp` | Rainbow rim width/intensity. `0.dp` = off (opt-in). `16.dp`–`32.dp` for a clear prism; `64.dp` for full-width rainbow. API 33+ only. |
| `hueShift` | `Float` | `0f` | Rotates the rainbow spectrum. `0f` = default colours; `0.5f` = 180° opposite; `1f` = full revolution back to start. Has no effect when `chromaticAberration == 0.dp`. |
| `tint` | `Color` | `Black @ 8%` | Translucent overlay. Default is a bare legibility scrim. White-alpha for frost. |
| `sheenAlpha` | `Float` | `0.12f` | Peak alpha of the diagonal white sheen. `0f` = off. |
| `borderAlpha` | `Float` | `0.65f` | Peak alpha of the 1-px specular border. `0f` = off. |
| `shape` | `Shape` | `RoundedCornerShape(16.dp)` | Outline used for the specular border. |

## Design notes

- **Why one modifier, two looks.** Clear glass and frosted glass share the same architecture (record into a shared `GraphicsLayer`, replay blurred and offset under the panel). The difference is purely whether you opt into the saturation, sheen, and border layers. One modifier with sensible defaults covers both.
- **Why the saturation pass exists.** Gaussian blur in linear RGB averages neighbouring pixels, which pulls colours toward the mean (i.e. toward grey). Without a re-saturation step the panel looks foggy regardless of how strong the blur is. CSS `backdrop-filter` recipes use `saturate(180%)` for the same reason; Apple's `UIVisualEffectView` does the same internally. This recipe chains an AGSL `RuntimeShader` (Rec. 709 luma + lerp toward original colour) ahead of `BlurEffect` on API 33+ via `RenderEffect.createChainEffect`. On API 31–32 the chain is skipped: the blur applies, but the panel will look slightly more muted.
- **How chromatic aberration works.** When `chromaticAberration > 0`, the blur + saturation + rainbow are all done in a single AGSL `RuntimeShader` applied to a *private* per-panel `GraphicsLayer` (never the shared source layer). The rainbow is two-part: a content channel refraction (R and B taps shifted radially outward/inward) for subtle physical realism, plus an additive angular rainbow (hue = `atan(dir)` mapped to full spectrum) that is always visible regardless of backdrop content. Both effects are gated by a `smoothstep` band that grows from a tight rim to ~48% of the panel as `chromaticAberration` increases. `hueShift` is simply added to the hue before the spectrum lookup.
- **Why a private layer.** `RenderNode.setRenderEffect` has no per-call snapshot: two `drawLayer` calls on the same layer in one draw scope share the effect at flush. A standalone `RuntimeShader` on the shared source layer never applies reliably. The private layer owned by the node side-steps this entirely.
- **Why the specular border is essential.** The single layer most often skipped. A 1-px gradient stroke (top-bright → side-fading) is what tells the eye that the panel has a *physical edge*. Without it, even a perfect blur reads as "darkened photo." On by default (`borderAlpha = 0.65f`, `sheenAlpha = 0.12f`); pass `0f` to either to suppress.
- **Compositing strategy.** `GlassState`'s init pins `compositingStrategy = Offscreen` on the shared `GraphicsLayer`. With `Auto`, the system can elide the offscreen buffer and `renderEffect` silently no-ops even on API 31+.
- **Cross-node invalidation.** Panels register with the state in `onAttach`; the source explicitly invalidates each panel's draw on every record. No snapshot-tracked tick (those loop under Robolectric).
- **Blur kernel vs. panel size.** A blur radius larger than the panel's shorter dimension averages every pixel to roughly the mean colour of the visible slice, defeating the purpose. Default `16.dp` is right for most caption-sized panels; bump to `24–40.dp` for taller panels.
- **Caller's clip.** The recipe doesn't clip the receiver; apply `Modifier.clip(shape)` *before* `Modifier.glass(state, shape = shape)` so the blurred backdrop and sheen stay inside the outline. The same `shape` is passed so the specular border stroke follows it.

## Performance

- One `GraphicsLayer` per state, one record/playback per frame. The blur and saturation passes run on the GPU.
- The shared layer is shared across multiple `Modifier.glass` panels keyed to the same `GlassState`, so several glass panels over the same source incur one record cost, not one per panel.
- The `RuntimeShader` for the saturation pass is created lazily on the first API-33+ draw and cached on the node; only the `amount` uniform is updated on parameter changes.
