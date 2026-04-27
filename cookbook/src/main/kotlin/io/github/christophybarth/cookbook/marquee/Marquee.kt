/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.marquee

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Direction of [marquee] travel. */
public enum class MarqueeDirection { LeftToRight, RightToLeft }

/**
 * Auto-scrolling marquee for content that overflows its container. Unlike Compose Foundation's
 * `basicMarquee`, this gives you direction, gap, fade-edge width, fade colour, and per-pixel
 * velocity control.
 *
 * The receiver is measured with relaxed width constraints so it can be wider than the container,
 * then translated horizontally on a fixed velocity. Two copies are drawn ([gap] apart) so the
 * loop is seamless. If the content fits within the container width, no animation runs; the
 * modifier becomes a no-op.
 *
 * The implementation chains a `graphicsLayer` with `CompositingStrategy.Offscreen` so the fade
 * mask (a `BlendMode.DstIn` gradient) lands on an isolated layer; without that, GPU canvases
 * collapse the fade into a plain coloured gradient. This is also why the modifier renders
 * correctly on every API level.
 *
 * **Modifier order tip:** the fade rect spans the marquee's measured height. Apply layout-shaping
 * modifiers like `padding` AFTER `marquee` if you want them included in the faded area, e.g.
 * `Modifier.background(c).marquee(...).padding(vertical = 8.dp)`.
 *
 * @param direction Travel direction.
 * @param velocityDpPerSec Pixels per second of horizontal scroll. `60.dp` is a comfortable read.
 * @param gap Gap between the trailing edge of one copy and the leading edge of the next.
 * @param fadeEdges Width of the fade-out gradient at the leading and trailing container edges.
 *   `0.dp` to disable.
 * @param fadeColor Colour the edges fade into. Defaults to [Color.Unspecified], which uses a true
 *   alpha mask so whatever is painted behind the marquee bleeds through. Pass an explicit colour
 *   when the marquee has no parent background and you want a clean fade to a known value (e.g.
 *   match the surrounding surface).
 *
 * @sample io.github.christophybarth.cookbook.samples.MarqueeSample
 */
public fun Modifier.marquee(
    direction: MarqueeDirection = MarqueeDirection.RightToLeft,
    velocityDpPerSec: Dp = 60.dp,
    gap: Dp = 32.dp,
    fadeEdges: Dp = 16.dp,
    fadeColor: Color = Color.Unspecified,
): Modifier {
    require(velocityDpPerSec.value > 0f) { "marquee: velocityDpPerSec must be > 0, was $velocityDpPerSec" }
    require(gap.value >= 0f) { "marquee: gap must be >= 0, was $gap" }
    require(fadeEdges.value >= 0f) { "marquee: fadeEdges must be >= 0, was $fadeEdges" }
    return this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .then(MarqueeElement(direction, velocityDpPerSec, gap, fadeEdges, fadeColor))
}

@Stable
private data class MarqueeElement(
    val direction: MarqueeDirection,
    val velocityDpPerSec: Dp,
    val gap: Dp,
    val fadeEdges: Dp,
    val fadeColor: Color,
) : ModifierNodeElement<MarqueeNode>() {
    override fun create(): MarqueeNode = MarqueeNode(direction, velocityDpPerSec, gap, fadeEdges, fadeColor)
    override fun update(node: MarqueeNode) {
        node.update(direction, velocityDpPerSec, gap, fadeEdges, fadeColor)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "marquee"
        properties["direction"] = direction
        properties["velocityDpPerSec"] = velocityDpPerSec
        properties["gap"] = gap
        properties["fadeEdges"] = fadeEdges
        properties["fadeColor"] = fadeColor
    }
}

internal class MarqueeNode(
    private var direction: MarqueeDirection,
    private var velocityDpPerSec: Dp,
    private var gap: Dp,
    private var fadeEdges: Dp,
    private var fadeColor: Color,
) : Modifier.Node(), DrawModifierNode, LayoutModifierNode {

    private val phase = Animatable(0f)
    private var animationJob: Job? = null
    private var contentWidthPx: Int = 0
    private var containerWidthPx: Int = 0
    private var gapPx: Float = 0f
    private var velocityPxPerSec: Float = 0f

    fun update(newDir: MarqueeDirection, newVelocity: Dp, newGap: Dp, newFade: Dp, newFadeColor: Color) {
        val velocityChanged = newVelocity != velocityDpPerSec
        val gapChanged = newGap != gap
        direction = newDir
        velocityDpPerSec = newVelocity
        gap = newGap
        fadeEdges = newFade
        fadeColor = newFadeColor
        if ((velocityChanged || gapChanged) && shouldScroll()) startAnimation()
        invalidateDraw()
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val relaxed = constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)
        val placeable = measurable.measure(relaxed)
        contentWidthPx = placeable.width
        containerWidthPx = if (constraints.hasBoundedWidth) constraints.maxWidth else placeable.width
        gapPx = gap.toPx()
        velocityPxPerSec = velocityDpPerSec.toPx()
        val height = placeable.height.coerceAtMost(constraints.maxHeight)
        if (shouldScroll() && animationJob == null) startAnimation()
        return layout(containerWidthPx, height) {
            placeable.place(0, 0)
        }
    }

    private fun shouldScroll(): Boolean = contentWidthPx > containerWidthPx

    private fun startAnimation() {
        if (!isAttached) return
        if (velocityPxPerSec <= 0f) return
        animationJob?.cancel()
        val totalTravel = (contentWidthPx + gapPx).coerceAtLeast(1f)
        val durationMs = (totalTravel / velocityPxPerSec * 1_000f).toInt().coerceAtLeast(1)
        animationJob = coroutineScope.launch {
            while (true) {
                phase.snapTo(0f)
                phase.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMs, easing = LinearEasing),
                ) {
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        if (!shouldScroll()) {
            drawContent()
            return
        }
        val totalTravel = contentWidthPx + gapPx
        val rawShift = phase.value * totalTravel
        val signedShift = if (direction == MarqueeDirection.RightToLeft) -rawShift else rawShift

        clipRectScope(left = 0f, top = 0f, right = size.width, bottom = size.height) {
            translateScope(signedShift, 0f) {
                drawContent()
            }
            val secondaryShift = if (direction == MarqueeDirection.RightToLeft) {
                signedShift + totalTravel
            } else {
                signedShift - totalTravel
            }
            translateScope(secondaryShift, 0f) {
                drawContent()
            }
        }

        val fadePx = fadeEdges.toPx()
        if (fadePx <= 0f) return
        if (fadeColor.isSpecified) drawSolidFade(fadePx) else drawAlphaMaskFade(fadePx)
    }

    private fun ContentDrawScope.drawAlphaMaskFade(fadePx: Float) {
        // True alpha mask: DstIn keeps destination pixels weighted by the source's alpha. The
        // graphicsLayer with CompositingStrategy.Offscreen above us is what makes this land on an
        // isolated buffer rather than collapsing on the GPU canvas.
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startX = 0f,
                endX = fadePx,
            ),
            topLeft = Offset.Zero,
            size = Size(fadePx, size.height),
            blendMode = BlendMode.DstIn,
        )
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Black, Color.Transparent),
                startX = size.width - fadePx,
                endX = size.width,
            ),
            topLeft = Offset(size.width - fadePx, 0f),
            size = Size(fadePx, size.height),
            blendMode = BlendMode.DstIn,
        )
    }

    private fun ContentDrawScope.drawSolidFade(fadePx: Float) {
        // Painted fade: a coloured gradient drawn on top of the marquee. Use this when the
        // marquee has no parent background to bleed through (or when you just want explicit
        // control of the fade-into colour).
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(fadeColor, Color.Transparent),
                startX = 0f,
                endX = fadePx,
            ),
            topLeft = Offset.Zero,
            size = Size(fadePx, size.height),
        )
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, fadeColor),
                startX = size.width - fadePx,
                endX = size.width,
            ),
            topLeft = Offset(size.width - fadePx, 0f),
            size = Size(fadePx, size.height),
        )
    }

    private inline fun ContentDrawScope.translateScope(dx: Float, dy: Float, block: ContentDrawScope.() -> Unit) {
        drawContext.transform.translate(dx, dy)
        block()
        drawContext.transform.translate(-dx, -dy)
    }

    private inline fun ContentDrawScope.clipRectScope(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        block: ContentDrawScope.() -> Unit,
    ) {
        drawContext.canvas.save()
        drawContext.canvas.clipRect(left, top, right, bottom)
        block()
        drawContext.canvas.restore()
    }

    override fun onAttach() {
        if (shouldScroll()) startAnimation()
    }

    override fun onDetach() {
        animationJob?.cancel()
        animationJob = null
    }
}
