# Writing your own modifier

A walk-through of the simplest modifier in the cookbook (`pressScale`) built up step by step. Once you've internalised the pattern here, the others are variations on a theme.

## What we're building

A modifier that scales the receiver down on press and springs it back on release. Public API:

```kotlin
fun Modifier.pressScale(
    scale: Float = 0.96f,
    animationSpec: AnimationSpec<Float> = DefaultPressSpring,
): Modifier
```

## Step 1: pick the right mixins

We need to:
1. Observe pointer events (down, up, cancel).
2. Apply a scale transform.

`PointerInputModifierNode` covers the first. For the transform we have two options:
- `DrawModifierNode` and apply the scale via `drawScope.scale { drawContent() }`.
- `LayoutModifierNode` and apply the scale via `placeable.placeWithLayer { scaleX = … }`.

The second is cheaper and uses the canonical layer system. We pick that.

## Step 2: write the public function

Validate inputs *outside* the node so callers see errors immediately, not at attach time:

```kotlin
fun Modifier.pressScale(
    scale: Float = DEFAULT_PRESSED_SCALE,
    animationSpec: AnimationSpec<Float> = DefaultPressSpring,
): Modifier {
    require(scale > 0f && scale <= 1f) { "pressScale: `scale` must be in (0f, 1f], was $scale" }
    return this then PressScaleElement(scale, animationSpec)
}
```

The function returns `this then Element(…)`, which is how Compose binds modifiers into a chain.

## Step 3: the `ModifierNodeElement`

This is the immutable carrier between composition and the node. `data class` gives us correct `equals` / `hashCode` for free, which Compose uses to decide whether to call `update()`:

```kotlin
private data class PressScaleElement(
    val scale: Float,
    val animationSpec: AnimationSpec<Float>,
) : ModifierNodeElement<PressScaleNode>() {

    override fun create(): PressScaleNode = PressScaleNode(scale, animationSpec)

    override fun update(node: PressScaleNode) {
        node.update(scale, animationSpec)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "pressScale"
        properties["scale"] = scale
    }
}
```

Three methods. `create()` is called once when the modifier is first attached. `update()` is called *every time the params change*, with the existing node. That's where you mutate, not recreate. `inspectableProperties` gives the layout inspector something useful to display.

## Step 4: the node

The node holds state (`isPressed`, the `Animatable`) and implements the chosen mixins:

```kotlin
internal class PressScaleNode(
    private var pressedScale: Float,
    private var animationSpec: AnimationSpec<Float>,
) : Modifier.Node(), PointerInputModifierNode, LayoutModifierNode {

    private val scaleValue = Animatable(1f)
    private var isPressed = false
    private var animationJob: Job? = null
```

The `Animatable` lives for the life of the node. Tasks launched via `coroutineScope.launch { … }` are cancelled automatically in `onDetach()`, so no manual cleanup is needed.

## Step 5: handle update

Re-target the in-flight animation rather than throwing it away:

```kotlin
fun update(newScale: Float, newSpec: AnimationSpec<Float>) {
    animationSpec = newSpec
    if (newScale != pressedScale) {
        pressedScale = newScale
        if (isPressed) runAnimation(pressedScale)
    }
}
```

If the user is currently pressing, the scale animation re-targets to the new value. If they're not, the next press picks up the new value. Either way: no node recreation, no animation hitch.

## Step 6: pointer handling

```kotlin
override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
    if (pass != PointerEventPass.Main) return
    val changes = pointerEvent.changes
    when (pointerEvent.type) {
        PointerEventType.Press -> if (!isPressed && changes.any { it.changedToDownIgnoreConsumed() }) {
            isPressed = true
            runAnimation(pressedScale)
        }
        PointerEventType.Release -> if (isPressed && changes.all { it.changedToUpIgnoreConsumed() }) {
            isPressed = false
            runAnimation(1f)
        }
        // out-of-bounds drag ⇒ release
    }
}

override fun onCancelPointerInput() {
    if (isPressed) {
        isPressed = false
        runAnimation(1f)
    }
}
```

We *observe* events; we never consume them. Downstream `clickable` and other gesture detectors keep working. `onCancelPointerInput` is the safety net: when a parent steals the gesture, we recover to flat.

## Step 7: apply the scale at placement

```kotlin
override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
    val placeable = measurable.measure(constraints)
    return layout(placeable.width, placeable.height) {
        placeable.placeWithLayer(x = 0, y = 0) {
            val s = scaleValue.value
            scaleX = s
            scaleY = s
            transformOrigin = TransformOrigin.Center
        }
    }
}

private fun runAnimation(target: Float) {
    animationJob?.cancel()
    animationJob = coroutineScope.launch {
        scaleValue.animateTo(target, animationSpec) {
            invalidateMeasurement()
        }
    }
}
```

Two things to note:
- `placeWithLayer` configures a `GraphicsLayer` for the child. The scale, transform origin, and any other layer properties are set inside the lambda.
- `invalidateMeasurement()` from inside the `animateTo` callback re-runs the placement on each animation frame. That's how the scale becomes visible. We don't trigger recomposition.

## What you've built

That's the whole pattern. Every modifier in the cookbook is the same shape:
- A factory `fun Modifier.x(...)` that validates and emits an element.
- A `ModifierNodeElement` data class with `create` / `update` / `inspectableProperties`.
- A node that mixes in the right interfaces, owns state, and drives invalidation through the appropriate `invalidate*` call.

When you find yourself reaching for `Modifier.composed`: stop, ask "where does this state want to live?", and write a node instead.
