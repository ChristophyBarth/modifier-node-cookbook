/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.revealonscroll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Fades and translates the receiver into view as it enters the window. Single-shot: once
 * revealed, the modifier sits idle and never animates again.
 *
 * The modifier is "in view" when more than [thresholdFraction] of its height is inside the
 * window. Increase [thresholdFraction] for a "fully visible" reveal, decrease for "any peek".
 *
 * @param translationY How far below the final position the receiver starts.
 * @param thresholdFraction Fraction of the receiver height that must be inside the window
 *   before the reveal fires. `0f` = any pixel, `1f` = fully visible.
 * @param animationSpec Spec used for the fade + translate.
 *
 * @sample io.github.christophybarth.cookbook.samples.RevealOnScrollSample
 */
public fun Modifier.revealOnScroll(
    translationY: Dp = 24.dp,
    thresholdFraction: Float = 0.4f,
    animationSpec: AnimationSpec<Float> = DefaultRevealSpec,
): Modifier {
    require(thresholdFraction in 0f..1f) {
        "revealOnScroll: thresholdFraction must be in [0,1], was $thresholdFraction"
    }
    return this then RevealOnScrollElement(translationY, thresholdFraction, animationSpec)
}

@Stable
private val DefaultRevealSpec: AnimationSpec<Float> = tween(durationMillis = 360)

private data class RevealOnScrollElement(
    val translationY: Dp,
    val thresholdFraction: Float,
    val animationSpec: AnimationSpec<Float>,
) : ModifierNodeElement<RevealOnScrollNode>() {
    override fun create(): RevealOnScrollNode =
        RevealOnScrollNode(translationY, thresholdFraction, animationSpec)
    override fun update(node: RevealOnScrollNode) {
        node.update(translationY, thresholdFraction, animationSpec)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "revealOnScroll"
        properties["translationY"] = translationY
        properties["thresholdFraction"] = thresholdFraction
    }
}

internal class RevealOnScrollNode(
    private var translationY: Dp,
    private var thresholdFraction: Float,
    private var animationSpec: AnimationSpec<Float>,
) : Modifier.Node(), LayoutModifierNode, GlobalPositionAwareModifierNode {

    private val progress = Animatable(0f)
    private var revealed = false
    private var animationJob: Job? = null

    fun update(newTranslationY: Dp, newThreshold: Float, newSpec: AnimationSpec<Float>) {
        translationY = newTranslationY
        thresholdFraction = newThreshold
        animationSpec = newSpec
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (revealed || !coordinates.isAttached) return
        val bounds = coordinates.boundsInWindow()
        val totalHeight = coordinates.size.height.toFloat().coerceAtLeast(1f)
        val visibleFraction = if (bounds.isEmpty) 0f else bounds.height.coerceAtLeast(0f) / totalHeight
        if (visibleFraction >= thresholdFraction) {
            revealed = true
            animationJob?.cancel()
            animationJob = coroutineScope.launch {
                progress.snapTo(0f)
                progress.animateTo(targetValue = 1f, animationSpec = animationSpec) {
                    invalidateMeasurement()
                }
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val t = progress.value
        val translateYPx = ((1f - t) * translationY.toPx()).toInt()
        val alpha = t
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(x = 0, y = translateYPx) {
                this.alpha = alpha
                transformOrigin = TransformOrigin.Center
            }
        }
    }
}
