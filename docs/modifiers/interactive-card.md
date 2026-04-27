# Modifier.interactiveCard

> Composite: `pressScale + tilt + hoverElevation + focusGlow` with sensible defaults.

## When to use

- Hero / showcase cards on landing screens.
- Premium tappable surfaces where four motion sources combine.
- The 80% case where you want "great-feeling card" without picking through individual modifier params.

## When _not_ to use

- Body lists or anywhere the cumulative motion would be busy.
- Surfaces that already use a Material `Card` with elevation tokens; the tokens and `interactiveCard`'s elevation will compete.

## Usage

```kotlin
Box(
    modifier = Modifier
        .size(140.dp)
        .focusable()
        .interactiveCard()
        .clip(RoundedCornerShape(16.dp))
        .background(brush)
        .clickable { onClick() },
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `pressedScale` | `Float` | `0.97f` | Subtler than `pressScale`'s standalone `0.96f`. |
| `tiltAngleDeg` | `Float` | `8f` | Half the standalone tilt default. |
| `restingElevation` | `Dp` | `2.dp` | Hoverable elevation rest. |
| `hoveredElevation` | `Dp` | `12.dp` | Hoverable elevation peak. |
| `focusGlowColor` | `Color` | indigo | Glow colour on focus. |
| `shape` | `Shape` | `RoundedCornerShape(16.dp)` | Outline used by hover-elevation's clipped layer. |

## Design notes

- **Why a composite?** None of the four underlying modifiers depends on the others; they just look great together. The composite is a single line for the common case and a stable signature to call from product code.
- **Tuning rationale:** combining four motion sources with each one's default is too busy. Halved tilt and tighter press-scale read as cohesive.
- **Order matters:** `focusGlow` first (renders the outline), then `hoverElevation` (lifts the layer), then `tilt` (rotates the lifted layer), then `pressScale` (scales the rotation). Reordering would change visual layering.
