# Modifier.bounceOnAppear

> Single-shot entry bounce when the node first attaches. After the animation completes, leaves the layer at scale `1f` and stays out of the way.

## When to use

- FABs, dialogs, toasts, snack bars, and other "pops into existence" UI.
- Composing with `AnimatedVisibility` to give the appear-side a tactile feel.

## When _not_ to use

- Items in a `LazyColumn` that scroll into view; the bounce will fire repeatedly as items recycle.
- Anywhere the user might otherwise miss what _is_ entering. Bounce is for emphasis, not for reveal.

## Usage

```kotlin
FloatingActionButton(
    onClick = { },
    modifier = Modifier.bounceOnAppear(),
) { Icon(Icons.Default.Add, contentDescription = null) }
```

To replay on demand, wrap in a `key()`:

```kotlin
key(replayCounter) {
    Box(modifier = Modifier.bounceOnAppear().background(Color.Blue))
}
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `initialScale` | `Float` | `0.6f` | Must be >= 0. `0f` = "snap into existence". |
| `spring` | `AnimationSpec<Float>` | medium-bouncy spring | Spec for the recovery to `1f`. |

## Design notes

- **Node subtype:** `LayoutModifierNode`. The `Animatable<Float>` is created at the initial scale and animated to `1f` in `onAttach()`. The scale is applied via `placeWithLayer { scaleX = … }`, so layout boxing isn't disturbed during the bounce; the node still reports its full measured size.
- **No re-trigger by default:** changing parameters mid-animation is intentionally not destructive; it takes effect on the _next_ attach. Mid-flight re-targeting would jank visibly.
- **Replay pattern:** wrap the composable in `key(value) { … }` and increment `value`; that detaches and re-attaches the node, re-firing the bounce.
- **Cleanup:** the underlying coroutine launches inside `coroutineScope` so it cancels automatically when the node detaches mid-animation.
