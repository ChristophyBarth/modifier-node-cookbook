# Modifier.hoverElevation

> Animates a card's shadow elevation in response to mouse / stylus hover.

## When to use

- Desktop, ChromeOS, foldable, and tablet cards that should "lift" when a pointer enters.
- Anywhere you want a hover affordance without plumbing a `MutableInteractionSource`.

## When _not_ to use

- Touch-only surfaces. There's no hover signal so the modifier sits idle. (It's safe to leave on regardless; just no benefit.)

## Usage

```kotlin
Box(
    modifier = Modifier
        .size(120.dp)
        .hoverElevation(restingDp = 2.dp, hoveredDp = 16.dp)
        .background(Color.White),
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `restingDp` | `Dp` | `2.dp` | Shadow when no pointer is over the composable. |
| `hoveredDp` | `Dp` | `12.dp` | Shadow when the pointer is hovering. |
| `shape` | `Shape` | `RoundedCornerShape(12.dp)` | Outline used to clip the elevated layer (controls shadow shape). |
| `animationSpec` | `AnimationSpec<Float>` | 180ms tween | Spec for the elevation interpolation. |

## Design notes

- **Node subtype:** `LayoutModifierNode + PointerInputModifierNode`. The elevation is applied via `placeWithLayer { shadowElevation = … }` so the shadow renders correctly on the layer's `GraphicsLayer`, including outside the layout bounds.
- **Why a node over composable wrappers?** Pointer hover state lives on the node and survives parent recomposition; a `Modifier.composed { … }` rebuild would lose the in-flight elevation animation.
- **Cancellation:** `onCancelPointerInput` resets `hovering` to `false` and animates back to rest, so an interrupted gesture (parent steals it) doesn't leave the card lifted.
