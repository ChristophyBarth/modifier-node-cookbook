# Modifier.loadingPlaceholder

> Composite: `skeleton + shimmer` in one call.

## When to use

- Any "content is loading" placeholder, anywhere.

## When _not_ to use

- See the individual modifier pages for the same caveats.

## Usage

```kotlin
Box(
    modifier = Modifier
        .size(width = 240.dp, height = 80.dp)
        .loadingPlaceholder(RoundedCornerShape(12.dp)),
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `shape` | `Shape` | `RoundedCornerShape(8.dp)` | Skeleton fill outline. |
| `color` | `Color` | light grey | Skeleton fill colour. |

## Design notes

- The composite is `this.skeleton(shape, color).shimmer()`. The skeleton paints first (replaces content with the placeholder shape), the shimmer paints on top (sweeps a highlight). Reverse the order and the highlight sits below the placeholder and never shows.
- For dark-theme users: pass a darker `color` and supply your own dark `shimmer` colour stops directly via the underlying `shimmer()` if you need fine control. The composite uses the default shimmer palette, which is tuned for light backgrounds.
