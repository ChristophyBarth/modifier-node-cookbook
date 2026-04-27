# Modifier.longPressProgress

> Draws a circular sweep progress arc over the receiver while a long-press is held; cancels on release before completion.

## When to use

- "Hold to confirm" destructive actions where a tap-to-confirm dialog feels too heavyweight (delete, leave call, end stream).
- Hold-to-record / hold-to-broadcast affordances.

## When _not_ to use

- Primary actions. Long-press is a discoverability liability; keep it for confirm-or-record gestures.

## Usage

```kotlin
Box(
    modifier = Modifier
        .size(64.dp)
        .background(Color(0xFFEF4444), CircleShape)
        .longPressProgress(durationMs = 1_000L) {
            confirmDelete()
        },
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `durationMs` | `Long` | `800L` | Hold time before [onComplete] fires. |
| `strokeWidth` | `Dp` | `4.dp` | Visual thickness of the arc. |
| `color` | `Color` | red | Arc colour. |
| `onComplete` | `() -> Unit` | _required_ | Called once when the press completes. Not called on release-before-complete or cancellation. |

## Design notes

- **Node subtype:** `DrawModifierNode + PointerInputModifierNode`. The arc renders inside the `draw()` block via `drawArc`. Pointer events drive a single `Animatable<Float>` from `0f` to `1f` over `durationMs`.
- **Cancellation:** if the press is released early, the in-flight `animateTo(1f)` is cancelled; we then animate back to `0f` over 160 ms. `onComplete` only fires when `animateTo(1f)` runs to completion, so it's safe to wire to destructive actions.
- **Out-of-bounds release:** if the pointer slides outside the bounds while held, treat it as a release (cancel). This matches platform long-press conventions and prevents accidental confirms when the user "drags off".
