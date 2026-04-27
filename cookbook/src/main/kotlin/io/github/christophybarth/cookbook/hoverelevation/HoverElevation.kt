/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.hoverelevation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Animates the receiver's drawn shadow elevation between [restingDp] and [hoveredDp] in response
 * to mouse / stylus hover. Pure pointer-based; no [androidx.compose.foundation.MutableInteractionSource]
 * plumbing required.
 *
 * Targets desktop, ChromeOS, large-screen Android with mouse, and foldables. On a touch-only
 * device, hover events are absent and the modifier sits at [restingDp] forever. Safe to leave on.
 *
 * Implementation note: the elevation is applied via `placeWithLayer { shadowElevation = … }` so
 * the layer / clip / shape live on the same `GraphicsLayer` and the shadow renders correctly
 * outside the layout bounds.
 *
 * @param restingDp Shadow when the pointer is not over the composable.
 * @param hoveredDp Shadow when the pointer enters and stays.
 * @param shape Outline used to clip the elevated layer (matters for shadow shape).
 * @param animationSpec Spec for the elevation interpolation; defaults to a 180ms tween.
 *
 * @sample io.github.christophybarth.cookbook.samples.HoverElevationSample
 */
public fun Modifier.hoverElevation(
    restingDp: Dp = 2.dp,
    hoveredDp: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    animationSpec: AnimationSpec<Float> = DefaultHoverSpec,
): Modifier {
    require(restingDp.value >= 0f) { "hoverElevation: restingDp must be >= 0, was $restingDp" }
    require(hoveredDp.value >= 0f) { "hoverElevation: hoveredDp must be >= 0, was $hoveredDp" }
    return this then HoverElevationElement(restingDp, hoveredDp, shape, animationSpec)
}

@Stable
private val DefaultHoverSpec: AnimationSpec<Float> = tween(durationMillis = 180)

private data class HoverElevationElement(
    val restingDp: Dp,
    val hoveredDp: Dp,
    val shape: Shape,
    val spec: AnimationSpec<Float>,
) : ModifierNodeElement<HoverElevationNode>() {
    override fun create(): HoverElevationNode = HoverElevationNode(restingDp, hoveredDp, shape, spec)
    override fun update(node: HoverElevationNode) {
        node.update(restingDp, hoveredDp, shape, spec)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "hoverElevation"
        properties["restingDp"] = restingDp
        properties["hoveredDp"] = hoveredDp
        properties["shape"] = shape
    }
}

internal class HoverElevationNode(
    private var restingDp: Dp,
    private var hoveredDp: Dp,
    private var shape: Shape,
    private var spec: AnimationSpec<Float>,
) : Modifier.Node(), PointerInputModifierNode, LayoutModifierNode {

    private val elevation = Animatable(restingDp.value)
    private var hovering = false
    private var animationJob: Job? = null

    fun update(newResting: Dp, newHovered: Dp, newShape: Shape, newSpec: AnimationSpec<Float>) {
        restingDp = newResting
        hoveredDp = newHovered
        shape = newShape
        spec = newSpec
        animateToTarget()
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (pass != PointerEventPass.Main) return
        when (pointerEvent.type) {
            PointerEventType.Enter -> if (!hovering) {
                hovering = true
                animateToTarget()
            }
            PointerEventType.Exit -> if (hovering) {
                hovering = false
                animateToTarget()
            }
        }
    }

    override fun onCancelPointerInput() {
        if (hovering) {
            hovering = false
            animateToTarget()
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val elevPx = elevation.value.dp.toPx()
        val outlineShape = shape
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                shadowElevation = elevPx
                this.shape = outlineShape
                clip = true
            }
        }
    }

    private fun animateToTarget() {
        val target = if (hovering) hoveredDp.value else restingDp.value
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            elevation.animateTo(target, spec) {
                invalidateMeasurement()
            }
        }
    }
}
