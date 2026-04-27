# Modifier.swipeToDismiss

> Drag past a threshold to dismiss; spring back otherwise. Horizontal or vertical.

## When to use

- Dismissable list rows (notifications, emails, conversations).
- Toast cards, snack bars, banner alerts.
- Full-screen sheets that should swipe-to-close.

## When _not_ to use

- Lists where horizontal swipe is reserved for reveal-actions (use [`magnetic`](magnetic.md) with anchors instead).
- Items inside a horizontally-scrolling parent; the gesture will fight the scroll.

## Usage

```kotlin
var rows by remember { mutableStateOf(listOf("Row 1", "Row 2", "Row 3")) }
rows.forEach { row ->
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .swipeToDismiss(onDismiss = { rows = rows - row })
            .background(MaterialTheme.colorScheme.primary),
    ) {
        Text(row)
    }
}
```

Vertical dismiss for a bottom sheet:

```kotlin
Modifier.swipeToDismiss(onDismiss = onClose, axis = SwipeAxis.Vertical)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `onDismiss` | `() -> Unit` | _required_ | Called once when a dismiss commits. |
| `threshold` | `Float` | `0.4f` | Drag distance, as fraction of receiver size, to commit. |
| `axis` | `SwipeAxis` | `Horizontal` | Drag axis. |

## Design notes

- **Node subtype:** `DelegatingNode + LayoutModifierNode`. We delegate to `SuspendingPointerInputModifierNode` and use `detectDragGestures` for the gesture loop. The `Animatable<Float>` tracks the offset and is applied via `placeable.place(x, y)` in `measure()`.
- **Why `DelegatingNode`?** It's the supertype that exposes the `delegate(...)` API. We can hand off pointer handling to a purpose-built suspending detector while still implementing `LayoutModifierNode` ourselves for placement.
- **Cancellation:** `onCancelPointerInput` (inherited via the delegate) and `onDragCancel` both spring back. Threshold check uses the receiver's measured size on the drag axis, so the same fractional threshold works for any element size.
- **Commit animation:** on commit we animate to ±`sizePx` (off-screen) before calling `onDismiss`. That's the visual "swept away" tell; the consumer's removal happens after the animation lands.
