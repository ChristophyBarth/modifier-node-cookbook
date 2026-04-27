/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.parallax

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

/**
 * Translates the receiver vertically by `scrollState.value * factor`, producing a parallax effect
 * relative to a parent scroll container.
 *
 * Apply this to a child placed _above_ or _within_ a scroll container. The child must not itself
 * be scrolled; if it were, this modifier and the inner scroll would fight.
 *
 * `factor` controls the parallax strength relative to the host scroll:
 * - `0f`: child is pinned to the screen (no parallax).
 * - `0.5f`: child moves at half the scroll speed (the canonical parallax look).
 * - `1f`: child moves at full scroll speed (no apparent depth).
 * - negative values move the child opposite to the scroll, producing a "popping forward" effect.
 *
 * Implementation: [ObserverModifierNode] reads `scrollState.value` from a snapshot observer; only
 * scroll deltas trigger remeasurement, never recompositions of the child or its siblings.
 *
 * @sample io.github.christophybarth.cookbook.samples.ParallaxSample
 */
public fun Modifier.parallax(
    scrollState: ScrollState,
    factor: Float = 0.5f,
): Modifier = this then ParallaxElement(scrollState, factor)

@Immutable
private data class ParallaxElement(
    val scrollState: ScrollState,
    val factor: Float,
) : ModifierNodeElement<ParallaxNode>() {
    override fun create(): ParallaxNode = ParallaxNode(scrollState, factor)
    override fun update(node: ParallaxNode) {
        node.update(scrollState, factor)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "parallax"
        properties["factor"] = factor
    }
}

internal class ParallaxNode(
    private var scrollState: ScrollState,
    private var factor: Float,
) : Modifier.Node(), LayoutModifierNode, ObserverModifierNode {

    private var observedScroll: Int = 0

    fun update(newState: ScrollState, newFactor: Float) {
        scrollState = newState
        factor = newFactor
        invalidateMeasurement()
    }

    override fun onAttach() {
        observe()
    }

    override fun onObservedReadsChanged() {
        observe()
    }

    private fun observe() {
        observeReads {
            val current = scrollState.value
            if (current != observedScroll) {
                observedScroll = current
                invalidateMeasurement()
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        // Negative: as scroll increases (content moves up) the background also moves up, but
        // slower than the foreground, producing depth. Positive would push it downward.
        val offsetY = -(observedScroll * factor).roundToInt()
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(x = 0, y = offsetY)
        }
    }
}
