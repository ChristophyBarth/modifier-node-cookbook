/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.magnetic

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Drag the receiver horizontally; on release it snaps to the nearest [anchors] position.
 *
 * Useful for slider-with-stops, "drag to choose category" pickers, or any input where the user
 * benefits from felt detents instead of a continuous range.
 *
 * @param anchors Anchor offsets in dp from the receiver's resting position. Must contain at
 *   least one entry; `0.dp` is included implicitly as the resting anchor.
 * @param snapThreshold Distance from an anchor (in dp) within which the modifier snaps. Larger
 *   values feel "stickier".
 *
 * @sample io.github.christophybarth.cookbook.samples.MagneticSample
 */
public fun Modifier.magnetic(
    anchors: List<Dp>,
    snapThreshold: Dp = 24.dp,
): Modifier {
    require(anchors.isNotEmpty()) { "magnetic: provide at least one anchor" }
    require(snapThreshold.value >= 0f) { "magnetic: snapThreshold must be >= 0, was $snapThreshold" }
    return this then MagneticElement(anchors, snapThreshold)
}

@Stable
private data class MagneticElement(
    val anchors: List<Dp>,
    val snapThreshold: Dp,
) : ModifierNodeElement<MagneticNode>() {
    override fun create(): MagneticNode = MagneticNode(anchors, snapThreshold)
    override fun update(node: MagneticNode) {
        node.update(anchors, snapThreshold)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "magnetic"
        properties["anchors"] = anchors
        properties["snapThreshold"] = snapThreshold
    }
}

internal class MagneticNode(
    private var anchors: List<Dp>,
    private var snapThreshold: Dp,
) : DelegatingNode(), LayoutModifierNode {

    private val offset = Animatable(0f)
    private var snapJob: Job? = null
    /** Latest density seen during measure, used to translate dp anchors to px on release. */
    private var lastDensityDp: Float = 1f

    init {
        delegate(
            SuspendingPointerInputModifierNode {
                detectDragGestures(
                    onDragStart = { snapJob?.cancel() },
                    onDragEnd = { snapToNearest() },
                    onDragCancel = { snapToNearest() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        snapJob?.cancel()
                        coroutineScope.launch {
                            offset.snapTo(offset.value + dragAmount.x)
                            invalidateMeasurement()
                        }
                    },
                )
            },
        )
    }

    fun update(newAnchors: List<Dp>, newThreshold: Dp) {
        anchors = newAnchors
        snapThreshold = newThreshold
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        lastDensityDp = density
        val ox = offset.value.toInt()
        return layout(placeable.width, placeable.height) {
            placeable.place(ox, 0)
        }
    }

    private fun snapToNearest() {
        snapJob?.cancel()
        snapJob = coroutineScope.launch {
            val anchorPixels = computeAnchorPixels()
            val current = offset.value
            val nearest = anchorPixels.minByOrNull { abs(it - current) } ?: 0f
            val withinThreshold = abs(nearest - current) <= snapThreshold.value * lastDensityDp
            val target = if (withinThreshold) nearest else current
            offset.animateTo(target) {
                invalidateMeasurement()
            }
        }
    }

    private fun computeAnchorPixels(): List<Float> {
        // Implicit `0.dp` anchor is the rest position, always allowed.
        val withRest = if (anchors.any { it.value == 0f }) anchors else anchors + 0.dp
        return withRest.map { it.value * lastDensityDp }
    }
}
