/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.shake

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Single-shot horizontal shake driven by a key. Each time [trigger] takes a new value (compared
 * via `equals`), the receiver shakes once with [oscillations] back-and-forth swings totalling
 * [durationMs]. Classic form-validation error tell.
 *
 * Use any value as a trigger key: an `Int` you increment, a `Throwable?`, an error sealed class.
 * Identical triggers do nothing; that's how you keep recompositions safe.
 *
 * @param trigger Any value; a change re-fires the shake.
 * @param intensity Peak horizontal displacement.
 * @param durationMs Total shake duration.
 * @param oscillations Number of full back-and-forth swings within [durationMs].
 *
 * @sample io.github.christophybarth.cookbook.samples.ShakeSample
 */
public fun Modifier.shake(
    trigger: Any?,
    intensity: Dp = 8.dp,
    durationMs: Int = 320,
    oscillations: Int = 3,
): Modifier {
    require(intensity.value >= 0f) { "shake: intensity must be >= 0, was $intensity" }
    require(durationMs > 0) { "shake: durationMs must be > 0, was $durationMs" }
    require(oscillations > 0) { "shake: oscillations must be > 0, was $oscillations" }
    return this then ShakeElement(trigger, intensity, durationMs, oscillations)
}

private data class ShakeElement(
    val trigger: Any?,
    val intensity: Dp,
    val durationMs: Int,
    val oscillations: Int,
) : ModifierNodeElement<ShakeNode>() {
    override fun create(): ShakeNode = ShakeNode(trigger, intensity, durationMs, oscillations)
    override fun update(node: ShakeNode) {
        node.update(trigger, intensity, durationMs, oscillations)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "shake"
        properties["intensity"] = intensity
        properties["durationMs"] = durationMs
        properties["oscillations"] = oscillations
    }
}

internal class ShakeNode(
    private var trigger: Any?,
    private var intensity: Dp,
    private var durationMs: Int,
    private var oscillations: Int,
) : Modifier.Node(), LayoutModifierNode {

    private val progress = Animatable(0f)
    private var animationJob: Job? = null

    fun update(newTrigger: Any?, newIntensity: Dp, newDuration: Int, newOscillations: Int) {
        val triggerChanged = newTrigger != trigger
        trigger = newTrigger
        intensity = newIntensity
        durationMs = newDuration
        oscillations = newOscillations
        if (triggerChanged && isAttached) fireShake()
    }

    private fun fireShake() {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMs, easing = LinearEasing),
            ) {
                invalidateMeasurement()
            }
            progress.snapTo(0f)
            invalidateMeasurement()
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val intensityPx = intensity.toPx()
        // sin(2π · oscillations · t) · intensity · damping
        val t = progress.value
        val damping = 1f - t
        val offsetPx = (sin(2.0 * PI * oscillations * t) * intensityPx * damping).toFloat()
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(x = offsetPx.toInt(), y = 0)
        }
    }
}
