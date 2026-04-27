/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.swipetodismiss

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/** Axis along which [swipeToDismiss] tracks drag. */
public enum class SwipeAxis { Horizontal, Vertical }

/**
 * Drags the receiver along [axis]. If the gesture passes [threshold] (as a fraction of the
 * receiver's size on that axis), [onDismiss] fires and the receiver animates fully off-screen.
 * Otherwise it springs back to the origin.
 *
 * Common product use: dismissable list rows, toast cards, snack bars, full-screen sheets.
 *
 * @param onDismiss Called once when a dismiss commits.
 * @param threshold Drag distance, as a fraction of the receiver's size on [axis], required to
 *   commit a dismiss. `0.4f` is a comfortable default.
 * @param axis Drag axis.
 *
 * @sample io.github.christophybarth.cookbook.samples.SwipeToDismissSample
 */
public fun Modifier.swipeToDismiss(
    onDismiss: () -> Unit,
    threshold: Float = 0.4f,
    axis: SwipeAxis = SwipeAxis.Horizontal,
): Modifier {
    require(threshold in 0f..1f) { "swipeToDismiss: threshold must be in [0,1], was $threshold" }
    return this then SwipeToDismissElement(onDismiss, threshold, axis)
}

@Stable
private data class SwipeToDismissElement(
    val onDismiss: () -> Unit,
    val threshold: Float,
    val axis: SwipeAxis,
) : ModifierNodeElement<SwipeToDismissNode>() {
    override fun create(): SwipeToDismissNode = SwipeToDismissNode(onDismiss, threshold, axis)
    override fun update(node: SwipeToDismissNode) {
        node.update(onDismiss, threshold, axis)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "swipeToDismiss"
        properties["threshold"] = threshold
        properties["axis"] = axis
    }
}

internal class SwipeToDismissNode(
    private var onDismiss: () -> Unit,
    private var threshold: Float,
    private var axis: SwipeAxis,
) : DelegatingNode(), LayoutModifierNode {

    private val offset = Animatable(0f)
    private var lastSizeOnAxis: Int = 0
    private var releaseJob: Job? = null

    init {
        delegate(
            SuspendingPointerInputModifierNode {
                detectDragGestures(
                    onDragStart = { releaseJob?.cancel() },
                    onDragEnd = { commitOrSpringBack() },
                    onDragCancel = { springBack() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val delta = if (axis == SwipeAxis.Horizontal) dragAmount.x else dragAmount.y
                        releaseJob?.cancel()
                        coroutineScope.launch {
                            offset.snapTo(offset.value + delta)
                            invalidateMeasurement()
                        }
                    },
                )
            },
        )
    }

    fun update(newOnDismiss: () -> Unit, newThreshold: Float, newAxis: SwipeAxis) {
        onDismiss = newOnDismiss
        threshold = newThreshold
        if (newAxis != axis) {
            axis = newAxis
            // Reset position when axis flips, otherwise the drag would visually carry over.
            coroutineScope.launch { offset.snapTo(0f) }
        }
    }

    private fun commitOrSpringBack() {
        val sizePx = lastSizeOnAxis.coerceAtLeast(1)
        val ratio = abs(offset.value) / sizePx
        if (ratio >= threshold) commitDismiss(sizePx) else springBack()
    }

    private fun commitDismiss(sizePx: Int) {
        val target = if (offset.value > 0f) sizePx.toFloat() else -sizePx.toFloat()
        releaseJob?.cancel()
        releaseJob = coroutineScope.launch {
            offset.animateTo(targetValue = target) {
                invalidateMeasurement()
            }
            onDismiss()
        }
    }

    private fun springBack() {
        releaseJob?.cancel()
        releaseJob = coroutineScope.launch {
            offset.animateTo(targetValue = 0f) {
                invalidateMeasurement()
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        lastSizeOnAxis = if (axis == SwipeAxis.Horizontal) placeable.width else placeable.height
        val ox = if (axis == SwipeAxis.Horizontal) offset.value.toInt() else 0
        val oy = if (axis == SwipeAxis.Vertical) offset.value.toInt() else 0
        return layout(placeable.width, placeable.height) {
            placeable.place(ox, oy)
        }
    }
}
