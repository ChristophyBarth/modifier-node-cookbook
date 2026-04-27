/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.shimmer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Sweeps a translucent gradient across the receiver to convey "loading" or "in-progress" state.
 *
 * Renders by drawing the underlying content first and then overlaying a linear gradient whose
 * translation animates along [angleDegrees]. This is the right approach for shimmer: layered
 * over real content the gradient reads as a highlight; layered over a [io.github
 * .christophybarth.cookbook.skeleton.skeleton] block it reads as a placeholder.
 *
 * The animation runs as long as the node is attached and is cancelled in `onDetach()` via the
 * node's coroutine scope. Hence: no leaked frames after the composable leaves the tree.
 *
 * The [angleDegrees] is mirrored automatically when the layout direction is RTL, so a 20° shimmer
 * sweeps in the visual reading direction in both LTR and RTL layouts.
 *
 * @param colors Gradient color stops the brush will cycle through. The default gives a soft
 *   highlight band over white-ish content; supply your own to match a darker theme.
 * @param angleDegrees Direction of travel, measured clockwise from the +X axis. `20f` reads as a
 *   gentle diagonal in both directions.
 * @param durationMillis Time for one full sweep across the bounds. Honors [LinearEasing] so the
 *   highlight moves at constant speed.
 * @param repeatMode How to loop. [RepeatMode.Restart] gives a continuous left-to-right sweep;
 *   [RepeatMode.Reverse] bounces back and forth.
 *
 * @sample io.github.christophybarth.cookbook.samples.ShimmerSample
 */
public fun Modifier.shimmer(
    colors: List<Color> = DefaultShimmerColors,
    angleDegrees: Float = 20f,
    durationMillis: Int = 1_400,
    repeatMode: RepeatMode = RepeatMode.Restart,
): Modifier {
    require(colors.size >= 2) { "shimmer: need at least two color stops, got ${colors.size}" }
    require(durationMillis > 0) { "shimmer: durationMillis must be positive, was $durationMillis" }
    return this then ShimmerElement(
        spec = ShimmerSpec(colors, angleDegrees, durationMillis, repeatMode),
    )
}

@Immutable
internal data class ShimmerSpec(
    val colors: List<Color>,
    val angleDegrees: Float,
    val durationMillis: Int,
    val repeatMode: RepeatMode,
)

/** Colors approximating Material 3 surface-tint shimmer on a light background. */
private val DefaultShimmerColors: List<Color> = listOf(
    Color(0x00CCCCCC),
    Color(0x66FFFFFF),
    Color(0x00CCCCCC),
)

private data class ShimmerElement(
    val spec: ShimmerSpec,
) : ModifierNodeElement<ShimmerNode>() {
    override fun create(): ShimmerNode = ShimmerNode(spec)
    override fun update(node: ShimmerNode) {
        node.update(spec)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "shimmer"
        properties["colors"] = spec.colors
        properties["angleDegrees"] = spec.angleDegrees
        properties["durationMillis"] = spec.durationMillis
        properties["repeatMode"] = spec.repeatMode
    }
}

internal class ShimmerNode(
    private var spec: ShimmerSpec,
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

    fun update(newSpec: ShimmerSpec) {
        val timingChanged =
            newSpec.durationMillis != spec.durationMillis || newSpec.repeatMode != spec.repeatMode
        spec = newSpec
        if (timingChanged && isAttached) {
            startAnimation()
        }
        invalidateDraw()
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (size.width == 0f || size.height == 0f) return

        // Sweep distance = bounds diagonal so the highlight clears the content fully each cycle.
        val travel = size.width + size.height
        val angleRad = Math.toRadians(directionalAngle(layoutDirection).toDouble())
        val dx = cos(angleRad).toFloat()
        val dy = sin(angleRad).toFloat()

        // Map [0,1] phase to a translation that starts off-screen on one side and ends off-screen
        // on the other. Offset components are not clamped; gradient tile mode handles the rest.
        val t = phase.value
        val shift = (t * 2f - 1f) * travel
        val start = Offset(-dx * travel + dx * shift, -dy * travel + dy * shift)
        val end = Offset(dx * travel + dx * shift, dy * travel + dy * shift)

        drawRect(
            brush = Brush.linearGradient(colors = spec.colors, start = start, end = end),
            blendMode = BlendMode.SrcOver,
        )
    }

    private fun directionalAngle(direction: LayoutDirection): Float =
        if (direction == LayoutDirection.Rtl) 180f - spec.angleDegrees else spec.angleDegrees

    private fun startAnimation() {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            phase.snapTo(0f)
            phase.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = spec.durationMillis, easing = LinearEasing),
                    repeatMode = spec.repeatMode,
                ),
            ) {
                invalidateDraw()
            }
        }
    }
}
