# Modifier.skeleton

> Replaces a composable's drawn content with an opaque placeholder shape.

## When to use

- The opaque base layer of any loading state. Pair with `Modifier.shimmer()` for motion.
- Layout placeholders where you know the rough geometry of the eventual content (image area, headline, body line).

## When _not_ to use

- When the content underneath is meaningful (skeleton hides it). For partial loads, use opacity instead.

## Usage

```kotlin
Box(
    modifier = Modifier
        .size(width = 240.dp, height = 80.dp)
        .skeleton(shape = RoundedCornerShape(12.dp))
        .shimmer(),
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `shape` | `Shape` | `RoundedCornerShape(8.dp)` | Outline used to clip and fill. |
| `color` | `Color` | light neutral grey | Fill colour. Switch to a darker tone for dark themes. |

## Design notes

- **Node subtype:** `DrawModifierNode`. We deliberately *don't* call `drawContent()` so the placeholder fully replaces the underlying content rather than painting over it. That avoids both ghosting and the cost of off-screen rendering of partly-loaded content.
- **Outline caching:** the shape's `Outline` is recomputed only when size, layout direction, or density changes. Stable in scroll lists.
- **Composition with `shimmer`:** order matters. `.skeleton().shimmer()` paints the shimmer over the placeholder. Reverse the order and the shimmer is hidden.
