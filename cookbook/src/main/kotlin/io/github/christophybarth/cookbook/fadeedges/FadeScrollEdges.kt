/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.fadeedges

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Fades the leading and trailing edges of a vertically-scrolling container based on the position
 * of [scrollState], so content visibly enters and exits the viewport rather than clipping abruptly.
 *
 * Apply this to the same composable that hosts the scroll, typically a `Column` with
 * `verticalScroll(scrollState)`, *outside* the scroll modifier so the fade applies to the visible
 * viewport rather than the scrolling content.
 *
 * Pattern in use: [ObserverModifierNode] reads [ScrollState.value] inside [observeReads] so the
 * draw invalidates only when the scroll position changes, never on every frame.
 *
 * @param scrollState The state driving the host's scroll. Reads from `value` and `maxValue`.
 * @param fadeLength Height of each fade band. The leading band shrinks to zero when scrolled to
 *   the top; the trailing band shrinks to zero when scrolled to the bottom.
 *
 * @sample io.github.christophybarth.cookbook.samples.FadeScrollEdgesSample
 */
public fun Modifier.fadeScrollEdges(
    scrollState: ScrollState,
    fadeLength: Dp = 24.dp,
): Modifier {
    require(fadeLength.value >= 0f) { "fadeScrollEdges: fadeLength must be >= 0, was $fadeLength" }
    return this then FadeScrollEdgesElement(scrollState, fadeLength)
}

@Immutable
private data class FadeScrollEdgesElement(
    val scrollState: ScrollState,
    val fadeLength: Dp,
) : ModifierNodeElement<FadeScrollEdgesNode>() {
    override fun create(): FadeScrollEdgesNode = FadeScrollEdgesNode(scrollState, fadeLength)
    override fun update(node: FadeScrollEdgesNode) {
        node.update(scrollState, fadeLength)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "fadeScrollEdges"
        properties["fadeLength"] = fadeLength
    }
}

internal class FadeScrollEdgesNode(
    private var scrollState: ScrollState,
    private var fadeLength: Dp,
) : Modifier.Node(), DrawModifierNode, LayoutModifierNode, ObserverModifierNode {

    private var observedTop: Float = 0f
    private var observedBottom: Float = 0f

    fun update(newState: ScrollState, newFade: Dp) {
        scrollState = newState
        fadeLength = newFade
        invalidateDraw()
    }

    override fun onAttach() {
        observeScroll()
    }

    override fun onObservedReadsChanged() {
        observeScroll()
    }

    private fun observeScroll() {
        observeReads {
            // Reading both `value` and `maxValue` registers them as observed snapshots.
            val top = scrollState.value.toFloat()
            val maxValue = scrollState.maxValue.toFloat()
            val bottom = (maxValue - top).coerceAtLeast(0f)
            if (top != observedTop || bottom != observedBottom) {
                observedTop = top
                observedBottom = bottom
                invalidateDraw()
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                // Offscreen compositing lets BlendMode.DstIn punch alpha through the content.
                compositingStrategy = CompositingStrategy.Offscreen
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (size.height <= 0f) return
        val maxFade = fadeLength.toPx()
        val topFade = min(maxFade, observedTop.coerceAtLeast(0f))
        val bottomFade = min(maxFade, observedBottom.coerceAtLeast(0f))

        if (topFade > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = topFade,
                ),
                topLeft = Offset.Zero,
                size = Size(size.width, topFade),
                blendMode = BlendMode.DstIn,
            )
        }
        if (bottomFade > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = size.height - bottomFade,
                    endY = size.height,
                ),
                topLeft = Offset(0f, size.height - bottomFade),
                size = Size(size.width, bottomFade),
                blendMode = BlendMode.DstIn,
            )
        }
    }
}
