/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.colorpulse

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.floor

/**
 * Cycles the receiver through a sequence of fill colours on a fixed period. Good for attention
 * markers: notification dots, "new" badges, recording indicators.
 *
 * The cycle pings between each adjacent pair of colours via [androidx.compose.ui.graphics.lerp]
 * over `durationMs / colors.size` per segment, with [FastOutSlowInEasing] for soft transitions.
 *
 * @param colors Fill colours to cycle through. Cycles back to the first after the last.
 * @param durationMs Time for one full pass through every colour.
 * @param shape Shape filled with the current colour.
 *
 * @sample io.github.christophybarth.cookbook.samples.ColorPulseSample
 */
public fun Modifier.colorPulse(
    colors: List<Color> = DefaultPulseColors,
    durationMs: Int = 1_600,
    shape: Shape = CircleShape,
): Modifier {
    require(colors.size >= 2) { "colorPulse: need at least two colors, got ${colors.size}" }
    require(durationMs > 0) { "colorPulse: durationMs must be > 0, was $durationMs" }
    return this then ColorPulseElement(colors, durationMs, shape)
}

@Stable
private val DefaultPulseColors: List<Color> = listOf(
    Color(0xFFEF4444),
    Color(0xFFF59E0B),
    Color(0xFFEF4444),
)

private data class ColorPulseElement(
    val colors: List<Color>,
    val durationMs: Int,
    val shape: Shape,
) : ModifierNodeElement<ColorPulseNode>() {
    override fun create(): ColorPulseNode = ColorPulseNode(colors, durationMs, shape)
    override fun update(node: ColorPulseNode) {
        node.update(colors, durationMs, shape)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "colorPulse"
        properties["colors"] = colors
        properties["durationMs"] = durationMs
        properties["shape"] = shape
    }
}

internal class ColorPulseNode(
    private var colors: List<Color>,
    private var durationMs: Int,
    private var shape: Shape,
) : Modifier.Node(), DrawModifierNode {

    private val phase = Animatable(0f)
    private var animationJob: Job? = null

    override fun onAttach() {
        startAnimation()
    }

    override fun onDetach() {
        animationJob?.cancel()
        animationJob = null
    }

    fun update(newColors: List<Color>, newDuration: Int, newShape: Shape) {
        val timingChanged = newDuration != durationMs
        colors = newColors
        durationMs = newDuration
        shape = newShape
        if (timingChanged && isAttached) startAnimation()
        invalidateDraw()
    }

    private fun startAnimation() {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            phase.snapTo(0f)
            phase.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMs, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            ) {
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (size.width <= 0f || size.height <= 0f) return
        val current = currentColor()
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutline(outline = outline, color = current)
    }

    private fun currentColor(): Color {
        val segments = colors.size
        val scaled = phase.value * segments
        val index = floor(scaled).toInt().coerceIn(0, segments - 1)
        val nextIndex = (index + 1) % segments
        val t = (scaled - index).coerceIn(0f, 1f)
        return lerp(colors[index], colors[nextIndex], t)
    }
}
