# Modifier.shimmer

> Sweeps a translucent gradient across the receiver to convey "loading" or "in-progress" state.

## When to use

- Skeleton loading states for cards, list rows, and image placeholders.
- Any time you want to signal "real content is on its way" without committing to a spinner.

## When _not_ to use

- Persistent / never-ending loads. Shimmer reads as "soon", not "forever". Switch to a determinate progress UI past ~3 seconds.
- Already-rendered content where the highlight will distract from real updates.

## Usage

Stack it on top of an opaque placeholder:

```kotlin
Box(
    modifier = Modifier
        .size(width = 240.dp, height = 16.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xFFE0E0E0))
        .shimmer(),
)
```

Customise the sweep colour for dark themes:

```kotlin
Modifier.shimmer(
    colors = listOf(
        Color(0x00FFFFFF),
        Color(0x33FFFFFF),
        Color(0x00FFFFFF),
    ),
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `colors` | `List<Color>` | three-stop white highlight | At least two stops. The middle stop is the highlight. |
| `angleDegrees` | `Float` | `20f` | Direction of travel. Mirrored automatically in RTL layouts. |
| `durationMillis` | `Int` | `1_400` | Time per sweep. Linear easing. |
| `repeatMode` | `RepeatMode` | `Restart` | `Reverse` ping-pongs back and forth. |

## Design notes

- **Node subtype:** `DrawModifierNode`. The animation is a single `Animatable<Float>` that runs on the node's `coroutineScope`, with `infiniteRepeatable(tween(durationMillis, LinearEasing))` driving the phase. The animation cancels in `onDetach()` and restarts in `onAttach()` so removing the composable from the tree stops drawing immediately.
- **No `LaunchedEffect`:** because the animation lifecycle is bound to node attach/detach, we never need a coroutine in composition. Adding `shimmer` to a composable that recomposes once a frame won't churn coroutine scopes.
- **RTL handling:** `angleDegrees` is mirrored across the vertical axis when `LocalLayoutDirection` is `Rtl`, so a 20° shimmer reads as "left-to-right" visually in both layouts.
- **Why redraw every frame?** The brush translation is computed from the current phase and the draw scope's `size`. We invalidate via `invalidateDraw()` from the `Animatable.animateTo` update lambda, never via state writes during draw, which would risk recomposition feedback.
- **Composition with `skeleton`:** `Modifier.shimmer()` paints over content. Pair it with `Modifier.skeleton(shape)` (also in the cookbook) when you need both an opaque placeholder shape *and* a sweep.
