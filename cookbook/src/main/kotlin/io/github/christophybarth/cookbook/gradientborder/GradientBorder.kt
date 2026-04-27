/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.gradientborder

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Draws an animated gradient stroke around the receiver. The gradient sweeps through its colour
 * stops on a fixed period; pass [animate] = false for a static border.
 *
 * The border is drawn _on top_ of content so it's never clipped by the child. Use [shape] to
 * match a rounded background; the stroke follows the shape's outline.
 *
 * @param colors Gradient stops. Two minimum.
 * @param width Stroke width.
 * @param shape Outline followed by the stroke.
 * @param durationMillis Time per full sweep when animating.
 * @param animate Whether to cycle the gradient.
 *
 * @sample io.github.christophybarth.cookbook.samples.GradientBorderSample
 */
public fun Modifier.gradientBorder(
    colors: List<Color> = DefaultGradientColors,
    width: Dp = 2.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    durationMillis: Int = 2_400,
    animate: Boolean = true,
): Modifier {
    require(colors.size >= 2) { "gradientBorder: need at least two colors, got ${colors.size}" }
    require(width.value >= 0f) { "gradientBorder: width must be >= 0, was $width" }
    require(durationMillis > 0) { "gradientBorder: durationMillis must be > 0, was $durationMillis" }
    return this then GradientBorderElement(colors, width, shape, durationMillis, animate)
}

@Stable
private val DefaultGradientColors: List<Color> = listOf(
    Color(0xFF6366F1),
    Color(0xFFEC4899),
    Color(0xFFF59E0B),
    Color(0xFF10B981),
    Color(0xFF6366F1),
)

private data class GradientBorderElement(
    val colors: List<Color>,
    val width: Dp,
    val shape: Shape,
    val durationMillis: Int,
    val animate: Boolean,
) : ModifierNodeElement<GradientBorderNode>() {
    override fun create(): GradientBorderNode =
        GradientBorderNode(colors, width, shape, durationMillis, animate)
    override fun update(node: GradientBorderNode) {
        node.update(colors, width, shape, durationMillis, animate)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "gradientBorder"
        properties["colors"] = colors
        properties["width"] = width
        properties["shape"] = shape
        properties["animate"] = animate
    }
}

internal class GradientBorderNode(
    private var colors: List<Color>,
    private var width: Dp,
    private var shape: Shape,
    private var durationMillis: Int,
    private var animate: Boolean,
) : Modifier.Node(), DrawModifierNode {

    private val phase = Animatable(0f)
    private var animationJob: Job? = null
    private var lastSize = androidx.compose.ui.geometry.Size.Zero
    private var cachedOutline: Outline? = null

    override fun onAttach() {
        if (animate) startAnimation()
    }

    override fun onDetach() {
        animationJob?.cancel()
        animationJob = null
    }

    fun update(
        newColors: List<Color>,
        newWidth: Dp,
        newShape: Shape,
        newDuration: Int,
        newAnimate: Boolean,
    ) {
        val animateChanged = newAnimate != animate
        val durationChanged = newDuration != durationMillis
        val shapeChanged = newShape != shape
        colors = newColors
        width = newWidth
        shape = newShape
        durationMillis = newDuration
        animate = newAnimate
        if (shapeChanged) cachedOutline = null
        if (animateChanged || durationChanged) {
            animationJob?.cancel()
            if (animate && isAttached) startAnimation()
        }
        invalidateDraw()
    }

    private fun startAnimation() {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            phase.snapTo(0f)
            phase.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis, easing = LinearEasing),
                ),
            ) {
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (size.width <= 0f || size.height <= 0f || width.toPx() <= 0f) return

        if (size != lastSize) {
            cachedOutline = null
            lastSize = size
        }
        val outline = cachedOutline
            ?: shape.createOutline(size, layoutDirection, this).also { cachedOutline = it }

        val travel = size.width + size.height
        val shift = phase.value * travel
        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(-travel + shift, -travel + shift),
            end = Offset(shift, shift),
        )
        drawOutline(outline = outline, brush = brush, style = Stroke(width = width.toPx()))
    }
}
