# Modifier.glassmorphism

> Frosted-glass effect over the receiver. Real backdrop blur on API 31+, translucent tint fallback below.

## When to use

- Floating panels and bottom sheets that should let underlying content read through.
- Hero overlays, lock-screen-style cards.
- Anywhere "iOS visual effect view" is the design reference.

## When _not_ to use

- API 21–30 is the dominant target and the design _depends on_ blur. The fallback is a flat tint, which is acceptable but not the same look.
- Behind opaque content. Without something to blur, the effect is invisible.

## Usage

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .background(heroImageBrush),
) {
    Box(
        modifier = Modifier
            .size(width = 240.dp, height = 100.dp)
            .clip(RoundedCornerShape(20.dp))
            .glassmorphism(
                blurRadius = 24.dp,
                tint = Color.White.copy(alpha = 0.18f),
            )
            .align(Alignment.Center),
    )
}
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `blurRadius` | `Dp` | `24.dp` | Blur strength on API 31+. Ignored on lower API levels. |
| `tint` | `Color` | white @ 18% | Always painted on top. Sole effect on API 21–30. |

## Design notes

- **Node subtype:** `LayoutModifierNode + DrawModifierNode`. The blur is applied via `placeWithLayer { renderEffect = RenderEffect.createBlurEffect(...).asComposeRenderEffect() }` on API 31+. The tint draws after `drawContent()`.
- **API gating:** `Build.VERSION.SDK_INT >= S` is the only branch needed; below that the layer simply has no `renderEffect` and the tint becomes the entire effect.
- **Why a separate tint?** Pure blur looks dirty in motion; a faint tint cleans up the colour cast. It also means the modifier degrades gracefully on older devices instead of doing nothing visible.
- **Compose with `clip`:** apply `clip(shape)` _before_ `glassmorphism` so the layer's blur respects the rounded outline, otherwise the blurred area extends past the clip.
