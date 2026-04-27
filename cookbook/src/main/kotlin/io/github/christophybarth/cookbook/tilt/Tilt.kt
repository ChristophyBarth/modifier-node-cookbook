/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.tilt

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 3D card-tilt that follows pointer position over the receiver. Tilts up to ±[maxAngleDeg] on
 * each axis, returning to flat when the pointer leaves.
 *
 * Tilt is mapped from the pointer's offset relative to the centre of the composable: a pointer
 * in the top-right tilts the card so its top-right is closest to the camera. The mapping is
 * linear, so the corners approach the maximum angle as the pointer reaches the edges.
 *
 * @param maxAngleDeg Maximum rotation in degrees on each axis. `12f` reads as a tasteful
 *   "responsive card" tilt; bump to `20f` for showcase / hero treatments.
 * @param animationSpec Spring used when recovering to flat on pointer exit.
 *
 * @sample io.github.christophybarth.cookbook.samples.TiltSample
 */
public fun Modifier.tilt(
    maxAngleDeg: Float = 12f,
    animationSpec: AnimationSpec<Float> = DefaultTiltSpec,
): Modifier {
    require(maxAngleDeg >= 0f) { "tilt: maxAngleDeg must be >= 0, was $maxAngleDeg" }
    return this then TiltElement(maxAngleDeg, animationSpec)
}

@Stable
private val DefaultTiltSpec: AnimationSpec<Float> = spring()

private data class TiltElement(
    val maxAngleDeg: Float,
    val spec: AnimationSpec<Float>,
) : ModifierNodeElement<TiltNode>() {
    override fun create(): TiltNode = TiltNode(maxAngleDeg, spec)
    override fun update(node: TiltNode) {
        node.update(maxAngleDeg, spec)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "tilt"
        properties["maxAngleDeg"] = maxAngleDeg
    }
}

internal class TiltNode(
    private var maxAngleDeg: Float,
    private var spec: AnimationSpec<Float>,
) : Modifier.Node(), PointerInputModifierNode, LayoutModifierNode {

    private val rotationX = Animatable(0f)
    private val rotationY = Animatable(0f)
    private var lastBounds: IntSize = IntSize.Zero
    private var recoveryJob: Job? = null

    fun update(newMax: Float, newSpec: AnimationSpec<Float>) {
        maxAngleDeg = newMax
        spec = newSpec
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (pass != PointerEventPass.Main) return
        lastBounds = bounds
        when (pointerEvent.type) {
            PointerEventType.Move, PointerEventType.Press, PointerEventType.Enter -> {
                val pos = pointerEvent.changes.firstOrNull()?.position ?: return
                snapToPosition(pos, bounds)
            }
            PointerEventType.Exit, PointerEventType.Release -> recoverToFlat()
        }
    }

    override fun onCancelPointerInput() = recoverToFlat()

    private fun snapToPosition(position: Offset, bounds: IntSize) {
        recoveryJob?.cancel()
        if (bounds.width <= 0 || bounds.height <= 0) return
        val centerX = bounds.width / 2f
        val centerY = bounds.height / 2f
        // Pointer left-of-centre tilts left edge towards camera ⇒ rotationY < 0.
        // Pointer above-centre tilts top edge towards camera ⇒ rotationX > 0.
        val targetY = ((position.x - centerX) / centerX) * maxAngleDeg
        val targetX = -((position.y - centerY) / centerY) * maxAngleDeg
        coroutineScope.launch {
            rotationY.snapTo(targetY.coerceIn(-maxAngleDeg, maxAngleDeg))
            invalidateMeasurement()
        }
        coroutineScope.launch {
            rotationX.snapTo(targetX.coerceIn(-maxAngleDeg, maxAngleDeg))
            invalidateMeasurement()
        }
    }

    private fun recoverToFlat() {
        recoveryJob?.cancel()
        recoveryJob = coroutineScope.launch {
            kotlinx.coroutines.coroutineScope {
                launch { rotationX.animateTo(0f, spec) { invalidateMeasurement() } }
                launch { rotationY.animateTo(0f, spec) { invalidateMeasurement() } }
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val rx = rotationX.value
        val ry = rotationY.value
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                rotationX = rx
                rotationY = ry
                cameraDistance = 12 * density
            }
        }
    }
}
