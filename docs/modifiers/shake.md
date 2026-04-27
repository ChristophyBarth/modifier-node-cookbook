# Modifier.shake

> Single-shot horizontal shake driven by a key. Re-fires when the key value changes.

## When to use

- Form validation errors. Shake the input on a failed submit.
- Wrong-PIN, wrong-password feedback before the field clears.

## When _not_ to use

- Persistent state changes (use `colorPulse` or a static error border for "stays wrong").
- During a typing or focus animation. Stack with `pressScale` only on inert fields.

## Usage

```kotlin
var error by remember { mutableStateOf<Throwable?>(null) }

OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    isError = error != null,
    modifier = Modifier.shake(trigger = error),
)
```

Or with a counter you increment on each failure:

```kotlin
var failures by remember { mutableIntStateOf(0) }

Modifier.shake(trigger = failures)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `trigger` | `Any?` | _required_ | Any value; a change re-fires the shake. Equal triggers do nothing. |
| `intensity` | `Dp` | `8.dp` | Peak horizontal displacement. |
| `durationMs` | `Int` | `320` | Total shake duration. |
| `oscillations` | `Int` | `3` | Number of full back-and-forth swings within `durationMs`. |

## Design notes

- **Node subtype:** `LayoutModifierNode`. Each frame we compute `sin(2π · oscillations · t) · intensity · damping` where `damping = 1f - t`, and apply the result as `placeRelative(x = offset)`. No `GraphicsLayer` involved; pure layout-time horizontal shift.
- **Key-driven:** the modifier compares the new `trigger` to the old one inside `update()`. If they differ, fire. If they're equal, do nothing, even if the modifier is recreated for unrelated reasons. This is the "single-shot via state key" pattern.
- **Damped:** the `1f - t` factor means the shake decays to zero rather than ending mid-swing. Cleaner than a hard cut-off.
