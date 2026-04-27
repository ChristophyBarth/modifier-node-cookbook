/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.skeleton

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Replaces the receiver's drawn content with an opaque shape, intended as a placeholder while
 * real content loads. Composes cleanly with [io.github.christophybarth.cookbook.shimmer.shimmer]
 * stacked on top to give the placeholder motion.
 *
 * The skeleton **replaces** content rather than painting over it; `drawContent()` is intentionally
 * not called. That keeps the placeholder cheap (no off-screen render of the real content) and
 * avoids "ghosting" where partly-loaded content peeks through.
 *
 * @param shape Outline used to clip and fill the placeholder. Defaults to an 8.dp rounded rect.
 * @param color Fill colour. The default is a light neutral that reads as "loading" on most
 *   surfaces; switch to a darker tone for dark themes.
 *
 * @sample io.github.christophybarth.cookbook.samples.SkeletonSample
 */
public fun Modifier.skeleton(
    shape: Shape = DefaultSkeletonShape,
    color: Color = DefaultSkeletonColor,
): Modifier = this then SkeletonElement(shape, color)

private val DefaultSkeletonShape: Shape = RoundedCornerShape(8.dp)
private val DefaultSkeletonColor: Color = Color(0xFFE0E0E0)

@Immutable
private data class SkeletonElement(
    val shape: Shape,
    val color: Color,
) : ModifierNodeElement<SkeletonNode>() {
    override fun create(): SkeletonNode = SkeletonNode(shape, color)
    override fun update(node: SkeletonNode) {
        node.update(shape, color)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "skeleton"
        properties["shape"] = shape
        properties["color"] = color
    }
}

internal class SkeletonNode(
    private var shape: Shape,
    private var color: Color,
) : Modifier.Node(), DrawModifierNode {

    private var lastSize: Size = Size.Zero
    private var lastDirection: LayoutDirection = LayoutDirection.Ltr
    private var lastDensity: Density? = null
    private var cachedOutline: Outline? = null

    fun update(newShape: Shape, newColor: Color) {
        if (newShape != shape) cachedOutline = null
        shape = newShape
        color = newColor
        invalidateDraw()
    }

    override fun ContentDrawScope.draw() {
        // Intentionally do not call drawContent(); the skeleton replaces content.
        if (size.width <= 0f || size.height <= 0f) return
        val outline = outlineFor(size, layoutDirection, this)
        drawOutline(outline = outline, color = color)
    }

    private fun outlineFor(
        currentSize: Size,
        direction: LayoutDirection,
        density: Density,
    ): Outline {
        val cached = cachedOutline
        val cacheValid = cached != null && cacheKeyMatches(currentSize, direction, density)
        if (cacheValid) return cached!!
        lastSize = currentSize
        lastDirection = direction
        lastDensity = density
        return shape.createOutline(currentSize, direction, density).also { cachedOutline = it }
    }

    private fun cacheKeyMatches(
        currentSize: Size,
        direction: LayoutDirection,
        density: Density,
    ): Boolean = currentSize == lastSize && direction == lastDirection && density == lastDensity
}
