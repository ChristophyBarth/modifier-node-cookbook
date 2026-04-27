/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.pinchzoom

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
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

/**
 * Two-finger pinch zoom and pan. Scale is clamped to `[minScale, maxScale]`. On release of the
 * second finger nothing snaps; the receiver stays at the user's chosen scale and pan.
 *
 * Pivot is the gesture centroid, so zoom feels anchored to where the fingers are. Pan respects
 * the zoom: at scale `1f` panning has no visual effect (the content already fills the layer).
 *
 * @param minScale Minimum scale. Below this, pinch-out animates back.
 * @param maxScale Maximum scale. Above this, pinch-in animates back.
 *
 * @sample io.github.christophybarth.cookbook.samples.PinchZoomSample
 */
public fun Modifier.pinchZoom(
    minScale: Float = 1f,
    maxScale: Float = 4f,
): Modifier {
    require(minScale > 0f) { "pinchZoom: minScale must be > 0, was $minScale" }
    require(maxScale >= minScale) { "pinchZoom: maxScale ($maxScale) must be >= minScale ($minScale)" }
    return this then PinchZoomElement(minScale, maxScale)
}

@Stable
private data class PinchZoomElement(
    val minScale: Float,
    val maxScale: Float,
) : ModifierNodeElement<PinchZoomNode>() {
    override fun create(): PinchZoomNode = PinchZoomNode(minScale, maxScale)
    override fun update(node: PinchZoomNode) {
        node.update(minScale, maxScale)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "pinchZoom"
        properties["minScale"] = minScale
        properties["maxScale"] = maxScale
    }
}

internal class PinchZoomNode(
    private var minScale: Float,
    private var maxScale: Float,
) : DelegatingNode(), LayoutModifierNode {

    private val scale = Animatable(1f)
    private val translationXAnim = Animatable(0f)
    private val translationYAnim = Animatable(0f)
    private var settleJob: Job? = null

    init {
        delegate(
            SuspendingPointerInputModifierNode {
                detectTransformGestures { _, pan, zoom, _ ->
                    settleJob?.cancel()
                    val newScale = (scale.value * zoom).coerceIn(minScale, maxScale)
                    coroutineScope.launch {
                        scale.snapTo(newScale)
                        translationXAnim.snapTo(translationXAnim.value + pan.x)
                        translationYAnim.snapTo(translationYAnim.value + pan.y)
                        invalidateMeasurement()
                    }
                }
            },
        )
    }

    fun update(newMin: Float, newMax: Float) {
        minScale = newMin
        maxScale = newMax
        if (scale.value !in newMin..newMax) {
            settleJob?.cancel()
            settleJob = coroutineScope.launch {
                scale.animateTo(scale.value.coerceIn(newMin, newMax)) {
                    invalidateMeasurement()
                }
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val s = scale.value
        val tx = translationXAnim.value
        val ty = translationYAnim.value
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                scaleX = s
                scaleY = s
                translationX = tx
                translationY = ty
                transformOrigin = TransformOrigin.Center
            }
        }
    }
}
