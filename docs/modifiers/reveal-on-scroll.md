# Modifier.revealOnScroll

> Fades and translates the receiver into view as it enters the window. Single-shot.

## When to use

- Long-form articles with images and captions that should appear as the reader reaches them.
- Marketing landing pages with section reveals.

## When _not_ to use

- Lists where every cell would reveal. If the user scrolls fast you'll see a wall of fade animations stacking.
- Above-the-fold content. The reveal fires immediately on attach when already visible, which can read as jarring on the first frame.

## Usage

```kotlin
Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
    repeat(6) { i ->
        Card(modifier = Modifier.revealOnScroll()) {
            // …
        }
    }
}
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `translationY` | `Dp` | `24.dp` | How far below the final position the receiver starts. |
| `thresholdFraction` | `Float` | `0.4f` | Fraction (0–1) of receiver height that must be inside the window to fire. |
| `animationSpec` | `AnimationSpec<Float>` | 360ms tween | Spec for the fade + translate. |

## Design notes

- **Node subtype:** `LayoutModifierNode + GlobalPositionAwareModifierNode`. We hook `onGloballyPositioned` (which fires whenever the receiver's window position changes), check the visible-fraction, and trigger the animation once.
- **Single-shot:** `revealed: Boolean` flag prevents re-fires. Once revealed, even if you scroll the receiver back out and in, it stays visible. This is intentional. The alternative (fade-out on exit, fade-in on re-entry) is `Modifier.fadeOnVisibility`, a different recipe.
- **Why `onGloballyPositioned` over an observer of the scroll state?** The modifier doesn't need to know which scroll container it lives in. Any scroll, any nesting depth, even a parent that translates for unrelated reasons will trigger the position update.
