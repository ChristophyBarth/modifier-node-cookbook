# Modifier.pressScale

> Scales the receiver down on press and springs it back to its original size on release.

## When to use

- Tap targets that need an immediate, tactile response (cards, tiles, large buttons).
- Anywhere you want a "pushed-in" feel without committing to a Material 3 ripple.

## When _not_ to use

- Inside lists where every row already animates (you'll fight existing motion).
- On composables with a `clickable` indication that already animates scale or elevation; pick one source of motion.

## Usage

```kotlin
Box(
    modifier = Modifier
        .size(96.dp)
        .pressScale()
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xFF6750A4))
        .clickable { onClick() },
)
```

Tighter shrink for primary CTAs:

```kotlin
Modifier.pressScale(scale = 0.9f)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `scale` | `Float` | `0.96f` | Target scale while pressed. Must be in `(0f, 1f]`. |
| `animationSpec` | `AnimationSpec<Float>` | medium-bouncy spring | Spring used for both shrink and recover. |

## Design notes

- **Node subtype:** `LayoutModifierNode` + `PointerInputModifierNode`. Pointer events drive a single long-lived `Animatable<Float>`. The scale is applied at placement time via `placeWithLayer { … }` rather than during `draw()`, which is cheaper and lets the layer system batch transforms.
- **Update vs. recreate:** changing `scale` while the node is attached re-targets any in-flight animation rather than recreating the node, so you can drive `scale` from state without dropping animation continuity.
- **Cancellation:** `onCancelPointerInput` and `onDetach` both reset to `1f` so a touch interrupted by a parent gesture or by leaving composition doesn't leave the layer stuck shrunk.
- **No `Indication` interaction:** the modifier observes pointer events with `PointerEventPass.Main` but does not consume them. Stack it with `clickable` freely.
