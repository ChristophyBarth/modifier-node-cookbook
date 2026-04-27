/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.dropshadow

import android.graphics.BlurMaskFilter
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * Draws a soft drop shadow under the receiver. Unlike `Modifier.shadow()`, the colour, blur, and
 * offset are independent of Material elevation, so designers get the shadow they actually drew.
 *
 * Renders by drawing a blurred outline behind the content, then `drawContent()` on top. The
 * outline is taken from [shape], so a `RoundedCornerShape` shadow follows the corner radius.
 *
 * @param color Shadow colour. Use a low alpha (e.g. `Color.Black.copy(alpha = 0.18f)`) for natural results.
 * @param blur Blur radius. `0.dp` produces a hard shadow.
 * @param offset Pixel offset of the shadow from the content. Positive Y = below, positive X = right.
 * @param shape Outline used for the shadow.
 *
 * @sample io.github.christophybarth.cookbook.samples.DropShadowSample
 */
public fun Modifier.dropShadow(
    color: Color = Color.Black.copy(alpha = 0.18f),
    blur: Dp = 12.dp,
    offset: DpOffset = DpOffset(x = 0.dp, y = 4.dp),
    shape: Shape = RectangleShape,
): Modifier {
    require(blur.value >= 0f) { "dropShadow: blur must be >= 0, was $blur" }
    return this then DropShadowElement(color, blur, offset, shape)
}

@Immutable
private data class DropShadowElement(
    val color: Color,
    val blur: Dp,
    val offset: DpOffset,
    val shape: Shape,
) : ModifierNodeElement<DropShadowNode>() {
    override fun create(): DropShadowNode = DropShadowNode(color, blur, offset, shape)
    override fun update(node: DropShadowNode) {
        node.update(color, blur, offset, shape)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "dropShadow"
        properties["color"] = color
        properties["blur"] = blur
        properties["offset"] = offset
        properties["shape"] = shape
    }
}

internal class DropShadowNode(
    private var color: Color,
    private var blur: Dp,
    private var offset: DpOffset,
    private var shape: Shape,
) : Modifier.Node(), DrawModifierNode {

    fun update(newColor: Color, newBlur: Dp, newOffset: DpOffset, newShape: Shape) {
        color = newColor
        blur = newBlur
        offset = newOffset
        shape = newShape
        invalidateDraw()
    }

    override fun ContentDrawScope.draw() {
        if (size.width <= 0f || size.height <= 0f || color.alpha <= 0f) {
            drawContent()
            return
        }
        val blurPx = blur.toPx()
        val outline = shape.createOutline(size, layoutDirection, this)
        val dx = offset.x.toPx()
        val dy = offset.y.toPx()

        val argb = color.toArgb()
        drawIntoCanvas { canvas ->
            val nativePaint = Paint().asFrameworkPaint().apply {
                this.color = argb
                if (blurPx > 0f) {
                    maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
                }
            }
            val native = canvas.nativeCanvas
            native.save()
            native.translate(dx, dy)
            when (outline) {
                is Outline.Rectangle -> native.drawRect(
                    outline.rect.left, outline.rect.top, outline.rect.right, outline.rect.bottom, nativePaint,
                )
                is Outline.Rounded -> {
                    val rr = outline.roundRect
                    native.drawRoundRect(
                        rr.left, rr.top, rr.right, rr.bottom,
                        rr.topLeftCornerRadius.x, rr.topLeftCornerRadius.y, nativePaint,
                    )
                }
                is Outline.Generic -> native.drawPath(outline.path.asAndroidPath(), nativePaint)
            }
            native.restore()
        }
        drawContent()
    }
}
