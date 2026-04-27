# Modifier.parallax

> Translates the receiver vertically by `scrollState.value * factor`, producing a parallax effect relative to a parent scroll.

## When to use

- Hero images that should move slower than the foreground.
- Decorative background layers in long-scroll articles or product pages.

## When _not_ to use

- Inside RecyclerView-equivalents where every row would parallax; it'd disorient the reader.

## Usage

```kotlin
val scroll = rememberScrollState()
Box {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .parallax(scroll, factor = 0.5f)
            .background(heroBrush),
    )
    Column(modifier = Modifier.verticalScroll(scroll).padding(top = 200.dp)) {
        // …foreground content…
    }
}
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `scrollState` | `ScrollState` | _required_ | The state driving the host's scroll. |
| `factor` | `Float` | `0.5f` | `0` = pinned, `0.5` = half speed, `1` = locked to scroll, negative = pops forward. |

## Design notes

- **Node subtype:** `LayoutModifierNode + ObserverModifierNode`. The scroll value is observed via `observeReads` so a remeasure happens only on scroll deltas, not when unrelated parents recompose.
- **No animation:** parallax tracks the input directly. If you want easing, wrap the offset in an `Animatable` upstream.
- **Composability:** the child must not also be scrolled. Combining `parallax` with an inner `verticalScroll` would fight. Stack the parallax above (z-order) the foreground, both reading the same `ScrollState`.
