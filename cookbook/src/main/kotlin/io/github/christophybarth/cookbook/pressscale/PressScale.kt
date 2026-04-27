/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.pressscale

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Scales the receiver down on press and springs it back to its original size on release.
 *
 * Why a node? `Modifier.composed { … }` would allocate a new state holder per composition tree
 * insertion and lose its animation across recompositions. A `Modifier.Node` keeps a single
 * long-lived [Animatable] tied to the node's lifecycle, so the animation continues smoothly
 * even while the parent recomposes and survives configuration changes via [update].
 *
 * The modifier intentionally does **not** consume pointer events. It observes them and lets
 * downstream gesture detectors (clicks, drags) keep working. Use this alongside `clickable`
 * without worrying about double-handling. Avoid stacking it with an [androidx.compose.foundation
 * .Indication] that already animates scale.
 *
 * @param scale Target scale while pressed. Must be in (0f, 1f]. Defaults to `0.96f`, a tap-down
 *   shrink that reads as responsive without becoming distracting.
 * @param animationSpec Spring used both to scale down and to recover. A medium-low stiffness
 *   reads as bouncy without overshooting visibly.
 *
 * @sample io.github.christophybarth.cookbook.samples.PressScaleSample
 */
public fun Modifier.pressScale(
    scale: Float = DEFAULT_PRESSED_SCALE,
    animationSpec: AnimationSpec<Float> = DefaultPressSpring,
): Modifier {
    require(scale > 0f && scale <= 1f) { "pressScale: `scale` must be in (0f, 1f], was $scale" }
    return this then PressScaleElement(scale, animationSpec)
}

private const val DEFAULT_PRESSED_SCALE = 0.96f

@Stable
private val DefaultPressSpring: AnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

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

/**
 * Internal node. Owns the [Animatable] driven by pointer events; applies the resulting scale via
 * `placeWithLayer`. We use [LayoutModifierNode] (not [androidx.compose.ui.node.DrawModifierNode])
 * because graphicsLayer transforms are configured at placement time, which is both cheaper and
 * the canonical way to integrate with the layer system.
 */
internal class PressScaleNode(
    private var pressedScale: Float,
    private var animationSpec: AnimationSpec<Float>,
) : Modifier.Node(), PointerInputModifierNode, LayoutModifierNode {

    private val scaleValue = Animatable(1f)
    private var isPressed = false
    private var animationJob: Job? = null

    fun update(newScale: Float, newSpec: AnimationSpec<Float>) {
        animationSpec = newSpec
        if (newScale != pressedScale) {
            pressedScale = newScale
            if (isPressed) {
                // Re-target an in-flight press animation rather than recreating the node.
                runAnimation(pressedScale)
            }
        }
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
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
            PointerEventType.Move -> if (isPressed && changes.any { it.isOutOfBounds(bounds, androidx.compose.ui.geometry.Size.Zero) }) {
                isPressed = false
                runAnimation(1f)
            }
        }
    }

    override fun onCancelPointerInput() {
        if (isPressed) {
            isPressed = false
            runAnimation(1f)
        }
    }

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
}
