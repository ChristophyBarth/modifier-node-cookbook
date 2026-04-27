# Modifier.marquee

> Auto-scroll content that overflows its container. Direction, gap, fade-edge width, and per-pixel velocity all configurable.

## Why over `basicMarquee`?

Compose Foundation ships `basicMarquee` for a simple text marquee. This recipe gives you:

- **Direction:** `LeftToRight` and `RightToLeft`.
- **Per-pixel velocity:** `velocityDpPerSec` lets you tune speed independent of content length.
- **Configurable gap:** the space between the trailing edge of one copy and the leading edge of the next.
- **Edge fades:** built-in `BlendMode.DstIn` gradient at both edges.
- **Auto no-op:** if content fits, no animation runs; the modifier becomes invisible.

## When to use

- Now-playing tickers, breaking news strips, scrolling badges.
- Long labels in narrow chips.

## When _not_ to use

- Body text. Marquee is for emphasis; reading text that scrolls is unkind.
- Content the user might need to interact with mid-scroll.

## Usage

```kotlin
Text(
    text = "Now playing: long song title that doesn't fit in the bar",
    modifier = Modifier
        .fillMaxWidth()
        .marquee(direction = MarqueeDirection.RightToLeft, fadeEdges = 16.dp),
    maxLines = 1,
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `direction` | `MarqueeDirection` | `RightToLeft` | Travel direction. |
| `velocityDpPerSec` | `Dp` | `60.dp` | Pixels per second of horizontal scroll. |
| `gap` | `Dp` | `32.dp` | Gap between the trailing edge of one copy and the leading edge of the next. |
| `fadeEdges` | `Dp` | `16.dp` | Width of fade-out gradient at container edges. `0.dp` to disable. |

## Design notes

- **Node subtype:** `LayoutModifierNode + DrawModifierNode`. Layout measures the child with relaxed width constraints (`Constraints.Infinity`) so the child can be wider than the container; we then layout at the container's width.
- **Two-copy seamless loop:** during draw, we translate by `phase × (contentWidth + gap)` and draw the child twice with the second copy offset by `(contentWidth + gap)`. As the first copy reaches the loop boundary the second is exactly in position to take over, no visible discontinuity.
- **Auto no-op:** if `contentWidthPx <= containerWidthPx`, `shouldScroll()` returns false and `draw()` calls `drawContent()` once with no translation. No animation is started.
- **Edge fades via `BlendMode.DstIn`:** the gradient's alpha multiplies the underlying alpha, fading the marquee content out at both edges. Requires offscreen compositing on the layer for the blend to apply only to our draw output.
