# Modifier.dropShadow

> Custom drop shadow with independent colour, blur, and offset; not tied to Material elevation.

## When to use

- Cards, sheets, FABs where the designer specified a shadow that doesn't match Material's elevation tokens.
- Coloured glow shadows (e.g. brand-tinted soft glow under a hero image).

## When _not_ to use

- Material 3 surfaces where you actually want the elevation token. Use `Modifier.shadow()` then.
- Performance-critical scrolling lists with many shadows. Each shadow is a `BlurMaskFilter`-painted draw. For large counts, consider a static shadow asset.

## Usage

```kotlin
Box(
    modifier = Modifier
        .size(120.dp)
        .dropShadow(
            color = Color.Black.copy(alpha = 0.18f),
            blur = 12.dp,
            offset = DpOffset(0.dp, 4.dp),
            shape = RoundedCornerShape(16.dp),
        )
        .clip(RoundedCornerShape(16.dp))
        .background(Color.White),
)
```

Coloured glow:

```kotlin
Modifier.dropShadow(
    color = Color(0xFF6366F1).copy(alpha = 0.45f),
    blur = 24.dp,
    offset = DpOffset.Zero,
    shape = RoundedCornerShape(16.dp),
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `color` | `Color` | black @ 18% | Use low alpha for natural results. |
| `blur` | `Dp` | `12.dp` | `0.dp` = hard shadow. |
| `offset` | `DpOffset` | `(0, 4.dp)` | Positive Y is below; positive X is right. |
| `shape` | `Shape` | `RectangleShape` | Outline drives the shadow shape. |

## Design notes

- **Node subtype:** `DrawModifierNode`. We grab the framework `Canvas` via `drawIntoCanvas` and paint a blurred outline using `BlurMaskFilter` (which is faster and more flexible than walking pixels), then call `drawContent()` on top.
- **Shape support:** rectangle, rounded rect, and generic `Path` outlines all work; the blur applies to the outline shape, so a rounded shadow follows the corner radius.
- **No layer trick:** unlike `Modifier.shadow()`, we don't enable a `GraphicsLayer` shadow on the parent. This means the shadow is purely cosmetic and won't interact with elevation overlays in dark theme. That's the point.
