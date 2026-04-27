# Modifier.tilt

> Subtle 3D card tilt that follows pointer position over the composable.

## When to use

- Hero / showcase cards on landing screens.
- Affordance for "this is a tappable, premium element"; pairs well with `pressScale`.

## When _not_ to use

- Body lists, list items, anywhere the user might mouse across many items quickly. The constant rotation is distracting.

## Usage

```kotlin
Box(
    modifier = Modifier
        .size(160.dp)
        .tilt(maxAngleDeg = 12f)
        .clip(RoundedCornerShape(16.dp))
        .background(brush),
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `maxAngleDeg` | `Float` | `12f` | Maximum rotation (degrees) on each axis. |
| `animationSpec` | `AnimationSpec<Float>` | spring | Used when recovering to flat on pointer exit. |

## Design notes

- **Node subtype:** `LayoutModifierNode + PointerInputModifierNode`. Pointer position is mapped linearly to `rotationX` / `rotationY`; on exit the node animates both back to `0f` via the configured spring.
- **`cameraDistance`:** set to `12 * density` so the perspective matches Material guidance for elevated surfaces. Adjust upstream if you nest this inside a `graphicsLayer { cameraDistance = … }` already.
- **Snap-vs-animate:** tracking is via `snapTo` while the pointer is moving (so the card sticks to the cursor) and `animateTo` only on recovery. Animating during tracking would feel laggy.
