/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.longpressprogress

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Draws a circular sweep progress arc over the receiver while a long-press is held. If the press
 * is released before [durationMs], the arc cancels and unwinds back to zero. If it completes,
 * [onComplete] fires exactly once.
 *
 * Useful for "hold to confirm" destructive actions where a tap-to-confirm dialog feels too
 * heavyweight, or for affordances like hold-to-record.
 *
 * @param durationMs Time the user must hold to fire [onComplete].
 * @param strokeWidth Visual thickness of the arc.
 * @param color Arc colour.
 * @param onComplete Called on the main thread when the press completes successfully. Not called
 *   on cancellation or release-before-complete.
 *
 * @sample io.github.christophybarth.cookbook.samples.LongPressProgressSample
 */
public fun Modifier.longPressProgress(
    durationMs: Long = DEFAULT_LONG_PRESS_MS,
    strokeWidth: Dp = 4.dp,
    color: Color = Color(0xFFEF4444),
    onComplete: () -> Unit,
): Modifier {
    require(durationMs > 0L) { "longPressProgress: durationMs must be > 0, was $durationMs" }
    require(strokeWidth.value >= 0f) { "longPressProgress: strokeWidth must be >= 0, was $strokeWidth" }
    return this then LongPressProgressElement(durationMs, strokeWidth, color, onComplete)
}

private const val DEFAULT_LONG_PRESS_MS: Long = 800L

@Stable
private data class LongPressProgressElement(
    val durationMs: Long,
    val strokeWidth: Dp,
    val color: Color,
    val onComplete: () -> Unit,
) : ModifierNodeElement<LongPressProgressNode>() {
    override fun create(): LongPressProgressNode =
        LongPressProgressNode(durationMs, strokeWidth, color, onComplete)
    override fun update(node: LongPressProgressNode) {
        node.update(durationMs, strokeWidth, color, onComplete)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "longPressProgress"
        properties["durationMs"] = durationMs
        properties["strokeWidth"] = strokeWidth
        properties["color"] = color
    }
}

internal class LongPressProgressNode(
    private var durationMs: Long,
    private var strokeWidth: Dp,
    private var color: Color,
    private var onComplete: () -> Unit,
) : Modifier.Node(), DrawModifierNode, PointerInputModifierNode {

    private val progress = Animatable(0f)
    private var pressJob: Job? = null
    private var pressed = false

    fun update(newDuration: Long, newStroke: Dp, newColor: Color, newOnComplete: () -> Unit) {
        durationMs = newDuration
        strokeWidth = newStroke
        color = newColor
        onComplete = newOnComplete
        invalidateDraw()
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (pass != PointerEventPass.Main) return
        val changes = pointerEvent.changes
        when (pointerEvent.type) {
            PointerEventType.Press -> if (!pressed && changes.any { it.changedToDownIgnoreConsumed() }) {
                pressed = true
                startProgress()
            }
            PointerEventType.Release -> if (pressed && changes.all { it.changedToUpIgnoreConsumed() }) {
                pressed = false
                cancelProgress()
            }
            PointerEventType.Move -> if (pressed && changes.any { it.isOutOfBounds(bounds, androidx.compose.ui.geometry.Size.Zero) }) {
                pressed = false
                cancelProgress()
            }
        }
    }

    override fun onCancelPointerInput() {
        if (pressed) {
            pressed = false
            cancelProgress()
        }
    }

    override fun onDetach() {
        pressJob?.cancel()
    }

    private fun startProgress() {
        pressJob?.cancel()
        pressJob = coroutineScope.launch {
            try {
                progress.snapTo(0f)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = durationMs.toInt(), easing = LinearEasing),
                ) {
                    invalidateDraw()
                }
                onComplete()
                progress.snapTo(0f)
                invalidateDraw()
            } catch (_: CancellationException) {
                // Released early; let cancelProgress handle the unwind.
            }
        }
    }

    private fun cancelProgress() {
        pressJob?.cancel()
        pressJob = coroutineScope.launch {
            progress.animateTo(0f, tween(durationMillis = 160, easing = LinearEasing)) {
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val p = progress.value
        if (p <= 0f) return

        val strokePx = strokeWidth.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(size.width - strokePx, size.height - strokePx)
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * p,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = strokePx),
        )
    }
}
