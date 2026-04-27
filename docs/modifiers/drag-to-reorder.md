# Modifier.dragToReorder

> Long-press a row, drag vertically; rows swap positions as the dragged item passes neighbours' midpoints.

## When to use

- Editable lists (todo apps, playlists, settings ordering, dashboard widgets).
- Anywhere users should rearrange items themselves.

## When _not_ to use

- Inside `LazyColumn`: animations and item-recycling complicate the model. Use a community library like `reorderable` for that.
- Lists where the order has semantic meaning the user can't change (e.g. timestamps).

## Usage

```kotlin
val state = rememberReorderableState(initial = listOf("Apples", "Bread", "Coffee"))

Column {
    state.items.forEach { item ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .dragToReorder(state, item)
                .background(Color.White),
        ) {
            Text(item)
        }
    }
}
```

## API

- **`ReorderableState<T>`**: mutable holder for the current item order. Read `state.items` to render rows.
- **`rememberReorderableState(initial: List<T>)`**: factory. Hold per `Composable`.
- **`Modifier.dragToReorder(state, key)`**: apply to each row, passing the row's stable key.

## Design notes

- **Node subtype:** `DelegatingNode + LayoutModifierNode`. Delegates to a `SuspendingPointerInputModifierNode` running `detectDragGesturesAfterLongPress` (matches Material's "press-and-hold to lift" convention). Layout records the row's height into the shared state and translates the row by `state.dragOffset` while it's the dragged one.
- **Mid-point swap:** during `onDrag`, if the accumulated offset exceeds half the row height, the state moves the dragged key one slot in `items` and rebases `dragOffset` by one slot in the opposite direction. Visually the finger stays "stuck" to the same y-coordinate; semantically the list reorders.
- **Lift visuals:** the dragged row gets a subtle `scaleX/Y = 1.02` and `shadowElevation = 12` via `placeRelativeWithLayer`. Cheap, no extra modifier needed.
- **Limitations (v0.2):** vertical only; no Lazy support; no animation of sibling translations as items shift. v0.3 work.
