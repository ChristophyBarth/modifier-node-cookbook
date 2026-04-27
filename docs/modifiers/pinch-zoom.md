# Modifier.pinchZoom

> Two-finger pinch zoom and pan. Scale clamped to `[minScale, maxScale]`.

## When to use

- Image viewers, map previews, document readers.
- Diagrams or canvases the user should explore.

## When _not_ to use

- Inside a parent with its own pinch / scroll gesture (the gestures will fight; pick one).
- For decorative elements where motion isn't meaningful.

## Usage

```kotlin
Image(
    painter = painterResource(R.drawable.hero),
    contentDescription = null,
    modifier = Modifier
        .fillMaxSize()
        .pinchZoom(minScale = 1f, maxScale = 5f),
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `minScale` | `Float` | `1f` | Scale floor. Pinch-out below clamps to this. |
| `maxScale` | `Float` | `4f` | Scale ceiling. |

## Design notes

- **Node subtype:** `DelegatingNode + LayoutModifierNode`. Delegates to `SuspendingPointerInputModifierNode` running `detectTransformGestures`, which gives `(centroid, pan, zoom, rotation)` per gesture frame.
- **Three `Animatable`s:** scale, translationX, translationY. All three are applied via `placeWithLayer { … }` so the transform happens on a single GPU layer; cheap and tidy.
- **No spring-back:** unlike `swipeToDismiss`, pinch-zoom does **not** snap back to `1f` on release. The user's chosen zoom persists. To opt into auto-reset, wrap in your own `LaunchedEffect` keyed on a "reset" trigger.
- **Bounds clamping:** if the user's pan would push the content fully off-screen, the modifier intentionally allows it (no clamp). Consumers wanting bounded pan should clip the parent and accept the partial visibility. Full bounded panning depends on knowing the displayed content's intrinsic size, which is out of scope for v0.2.
