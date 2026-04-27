/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.bounceonappear

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.launch

/**
 * Single-shot entry bounce: the receiver scales up from [initialScale] to `1f` via a bouncy
 * spring when the node first attaches. After the animation completes nothing else happens.
 * The node leaves the layer at scale `1f` and stays out of the way.
 *
 * Useful for FABs, dialogs, toasts, or anything that should "pop" into view.
 *
 * @param initialScale Starting scale. Must be >= 0. `0f` reads as a "snap into existence" feel.
 * @param spring Spring used for the recovery to `1f`.
 *
 * @sample io.github.christophybarth.cookbook.samples.BounceOnAppearSample
 */
public fun Modifier.bounceOnAppear(
    initialScale: Float = 0.6f,
    spring: AnimationSpec<Float> = DefaultBounceSpring,
): Modifier {
    require(initialScale >= 0f) { "bounceOnAppear: initialScale must be >= 0, was $initialScale" }
    return this then BounceOnAppearElement(initialScale, spring)
}

@Stable
private val DefaultBounceSpring: AnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

private data class BounceOnAppearElement(
    val initialScale: Float,
    val spring: AnimationSpec<Float>,
) : ModifierNodeElement<BounceOnAppearNode>() {
    override fun create(): BounceOnAppearNode = BounceOnAppearNode(initialScale, spring)
    override fun update(node: BounceOnAppearNode) {
        node.update(initialScale, spring)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "bounceOnAppear"
        properties["initialScale"] = initialScale
    }
}

internal class BounceOnAppearNode(
    private var initialScale: Float,
    private var animationSpec: AnimationSpec<Float>,
) : Modifier.Node(), LayoutModifierNode {

    private val scale = Animatable(initialScale)

    override fun onAttach() {
        coroutineScope.launch {
            scale.snapTo(initialScale)
            scale.animateTo(targetValue = 1f, animationSpec = animationSpec) {
                invalidateMeasurement()
            }
        }
    }

    fun update(newInitial: Float, newSpec: AnimationSpec<Float>) {
        // Updates take effect on the next attach. Mid-animation re-targets would jank.
        initialScale = newInitial
        animationSpec = newSpec
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val s = scale.value
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                scaleX = s
                scaleY = s
                transformOrigin = TransformOrigin.Center
            }
        }
    }
}
