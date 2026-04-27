# Modifier.fadeScrollEdges

> Fades the leading and trailing edges of a scrollable container based on scroll position.

## When to use

- Long-form content surfaces where a hard clip at the top/bottom looks abrupt (article views, settings sheets, comment lists).
- Bottom sheets and modals where edges should signal "more content this way".

## When _not_ to use

- Containers that already have visible chrome at their edges (toolbars, snack bars). The fade reads as inconsistent.

## Usage

Outside the `verticalScroll`, around the same composable:

```kotlin
val scroll = rememberScrollState()
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(220.dp)
        .fadeScrollEdges(scroll),
) {
    Column(modifier = Modifier.verticalScroll(scroll)) {
        // …content…
    }
}
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `scrollState` | `ScrollState` | _required_ | The state driving the host's scroll. |
| `fadeLength` | `Dp` | `24.dp` | Height of each fade band. Shrinks to zero at the end of the scroll range. |

## Design notes

- **Node subtype:** `DrawModifierNode + LayoutModifierNode + ObserverModifierNode`. The observer reads `scrollState.value` and `scrollState.maxValue` inside `observeReads` so only scroll changes trigger redraws, not unrelated recompositions.
- **Why offscreen compositing?** The fade uses `BlendMode.DstIn` to multiply the content's alpha by the gradient. That requires the node's contents to be on a separate compositing layer, which we set via `placeWithLayer { compositingStrategy = Offscreen }`.
- **RTL safety:** vertical fades are direction-agnostic. For a horizontal variant, mirror the gradient based on `LocalLayoutDirection`.
