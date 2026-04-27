# Modifier.gradientBorder

> Animated gradient stroke around the receiver. Static mode supported.

## When to use

- Premium / pro / pro-tier badges and feature highlights.
- "Live" indicators where motion matters.
- Loading borders ("processing your upload…") as a richer alternative to a spinner.

## When _not_ to use

- Body text or anywhere the animation would be visually noisy.
- Atop content that already pulses (you'll fight the pulse).

## Usage

```kotlin
Box(
    modifier = Modifier
        .size(96.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xFF111827))
        .gradientBorder(width = 3.dp, shape = RoundedCornerShape(16.dp)),
)
```

Static border (no animation):

```kotlin
Modifier.gradientBorder(animate = false)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `colors` | `List<Color>` | rainbow indigo / pink / amber / emerald | Two minimum. Cycle is implicit. |
| `width` | `Dp` | `2.dp` | Stroke width. |
| `shape` | `Shape` | `RoundedCornerShape(12.dp)` | Outline followed by the stroke. |
| `durationMillis` | `Int` | `2_400` | Time per full sweep when animating. |
| `animate` | `Boolean` | `true` | Set `false` for a static gradient stroke. |

## Design notes

- **Node subtype:** `DrawModifierNode`. The brush is a `Brush.linearGradient` whose `start` and `end` are translated by the animated phase. We cache the shape `Outline` and only invalidate when the size changes.
- **Drawn _after_ content:** the stroke renders on top of `drawContent()`, so it is never clipped by the child's background.
- **Update without recreate:** changing `animate` or `durationMillis` cancels and restarts the animation from `0f` rather than recreating the node, so colour and width changes hit immediately without visual jumps.
