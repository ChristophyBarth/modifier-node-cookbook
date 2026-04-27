/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.glassmorphism

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
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

/**
 * Frosted-glass effect over the receiver. On API 31+ uses [RenderEffect.createBlurEffect] for a
 * real backdrop blur; on lower API levels falls back to a translucent [tint] overlay so the
 * effect at least communicates "glass surface" visually.
 *
 * Apply this to a composable that sits _on top of_ the content you want blurred. The blur acts
 * on whatever this layer renders; combine with a [androidx.compose.foundation.background] or
 * stacked imagery to get the classic frosted look.
 *
 * @param blurRadius Blur radius applied on API 31+.
 * @param tint Translucent tint always painted on top (gives the glass its colour cast and acts
 *   as the sole effect on API 21–30).
 *
 * @sample io.github.christophybarth.cookbook.samples.GlassmorphismSample
 */
public fun Modifier.glassmorphism(
    blurRadius: Dp = 24.dp,
    tint: Color = Color.White.copy(alpha = 0.18f),
): Modifier {
    require(blurRadius.value >= 0f) { "glassmorphism: blurRadius must be >= 0, was $blurRadius" }
    return this then GlassmorphismElement(blurRadius, tint)
}

@Stable
private data class GlassmorphismElement(
    val blurRadius: Dp,
    val tint: Color,
) : ModifierNodeElement<GlassmorphismNode>() {
    override fun create(): GlassmorphismNode = GlassmorphismNode(blurRadius, tint)
    override fun update(node: GlassmorphismNode) {
        node.update(blurRadius, tint)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "glassmorphism"
        properties["blurRadius"] = blurRadius
        properties["tint"] = tint
    }
}

internal class GlassmorphismNode(
    private var blurRadius: Dp,
    private var tint: Color,
) : Modifier.Node(), DrawModifierNode, LayoutModifierNode {

    fun update(newBlur: Dp, newTint: Color) {
        blurRadius = newBlur
        tint = newTint
        invalidateDraw()
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val px = blurRadius.toPx().coerceAtLeast(0.01f)
                    renderEffect = RenderEffect
                        .createBlurEffect(px, px, Shader.TileMode.CLAMP)
                        .asComposeRenderEffect()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (tint.alpha > 0f) {
            drawRect(color = tint)
        }
    }
}
