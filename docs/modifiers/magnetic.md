# Modifier.magnetic

> Drag horizontally; on release, snap to the nearest anchor.

## When to use

- Drawer-style "swipe to reveal actions" rows where settled positions are 0 / partial / full.
- Slider-with-stops inputs (volume presets, intensity levels).
- "Drag the chip into a category" gestures.

## When _not_ to use

- Continuous-range sliders (use `Slider` from Material).
- Gestures where overshoot/elastic feel matters more than discreteness.

## Usage

```kotlin
Box(
    modifier = Modifier
        .size(48.dp)
        .magnetic(anchors = listOf((-120).dp, 0.dp, 120.dp))
        .background(Color.Blue, CircleShape),
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `anchors` | `List<Dp>` | _required_ | Offsets from rest. `0.dp` is included implicitly as a rest anchor. |
| `snapThreshold` | `Dp` | `24.dp` | Distance from an anchor within which snap occurs. Larger = stickier. |

## Design notes

- **Node subtype:** `DelegatingNode + LayoutModifierNode`. Delegates to a `SuspendingPointerInputModifierNode` running `detectDragGestures`. On release, we find the nearest anchor in pixel-space and `Animatable.animateTo` it.
- **Density handling:** the modifier needs density to convert the dp-typed anchors to pixels at snap time. We snapshot density inside `measure()` (where the `MeasureScope` provides it) and use the captured value at gesture-end time.
- **Implicit rest anchor:** if your anchor list doesn't include `0.dp`, the modifier adds it. Lets you write `anchors = listOf(120.dp)` for a single non-zero detent and still get spring-back.
- **No haptic by default:** intentional. Wire your own `HapticFeedback.performHapticFeedback(LongPress)` from a state observer if you want a tap-on-snap.
