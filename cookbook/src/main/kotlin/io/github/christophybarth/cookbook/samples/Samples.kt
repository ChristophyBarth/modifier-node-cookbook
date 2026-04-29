/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.samples

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.bounceonappear.bounceOnAppear
import io.github.christophybarth.cookbook.colorpulse.colorPulse
import io.github.christophybarth.cookbook.composites.interactiveCard
import io.github.christophybarth.cookbook.composites.loadingPlaceholder
import io.github.christophybarth.cookbook.dragtoreorder.dragToReorder
import io.github.christophybarth.cookbook.dragtoreorder.rememberReorderableState
import io.github.christophybarth.cookbook.dropshadow.dropShadow
import io.github.christophybarth.cookbook.fadeedges.fadeScrollEdges
import io.github.christophybarth.cookbook.glassmorphism.glassmorphism
import io.github.christophybarth.cookbook.gradientborder.gradientBorder
import io.github.christophybarth.cookbook.hoverelevation.hoverElevation
import io.github.christophybarth.cookbook.longpressprogress.longPressProgress
import io.github.christophybarth.cookbook.magnetic.magnetic
import io.github.christophybarth.cookbook.marquee.marquee
import io.github.christophybarth.cookbook.parallax.parallax
import io.github.christophybarth.cookbook.pinchzoom.pinchZoom
import io.github.christophybarth.cookbook.pressscale.pressScale
import io.github.christophybarth.cookbook.revealonscroll.revealOnScroll
import io.github.christophybarth.cookbook.shake.shake
import io.github.christophybarth.cookbook.shimmer.shimmer
import io.github.christophybarth.cookbook.skeleton.skeleton
import io.github.christophybarth.cookbook.swipetodismiss.swipeToDismiss
import io.github.christophybarth.cookbook.tilt.tilt

@Composable public fun PressScaleSample() {
    Box(Modifier.size(96.dp).pressScale().clip(RoundedCornerShape(16.dp)).background(Color(0xFF6750A4)))
}

@Composable public fun ShimmerSample() {
    Box(
        Modifier
            .size(width = 240.dp, height = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE0E0E0))
            .shimmer(),
    )
}

@Composable public fun SkeletonSample() {
    // shimmer before skeleton: skeleton does not drawContent(), so anything after it is dead.
    Box(Modifier.size(width = 200.dp, height = 64.dp).shimmer().skeleton(RoundedCornerShape(12.dp)))
}

@Composable public fun HoverElevationSample() {
    Box(Modifier.size(120.dp).hoverElevation(restingDp = 2.dp, hoveredDp = 16.dp).background(Color.White))
}

@Composable public fun FadeScrollEdgesSample() {
    val scroll = remember { ScrollState(0) }
    Column(
        modifier = Modifier
            .size(width = 200.dp, height = 200.dp)
            .fadeScrollEdges(scroll)
            .verticalScroll(scroll),
    ) {
        repeat(20) { Box(Modifier.size(width = 200.dp, height = 24.dp).background(Color(0xFFB0BEC5))) }
    }
}

@Composable public fun ParallaxSample() {
    val scroll = remember { ScrollState(0) }
    Column(modifier = Modifier.size(width = 200.dp, height = 240.dp).verticalScroll(scroll)) {
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 80.dp)
                .parallax(scroll, factor = 0.5f)
                .background(Color(0xFF6750A4)),
        )
        repeat(10) { Box(Modifier.size(width = 200.dp, height = 32.dp).background(Color.LightGray)) }
    }
}

@Composable public fun TiltSample() {
    Box(Modifier.size(160.dp).tilt().clip(RoundedCornerShape(16.dp)).background(Color(0xFF21C386)))
}

@Composable public fun LongPressProgressSample() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .longPressProgress(durationMs = 1_000L) { /* fire delete */ }
            .background(Color(0xFFEF4444), RoundedCornerShape(48.dp)),
    )
}

@Composable public fun DropShadowSample() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .dropShadow(
                color = Color.Black.copy(alpha = 0.2f),
                blur = 16.dp,
                offset = DpOffset(0.dp, 6.dp),
                shape = RoundedCornerShape(16.dp),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
    )
}

@Composable public fun GradientBorderSample() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111827))
            .gradientBorder(width = 3.dp, shape = RoundedCornerShape(16.dp)),
    )
}

@Composable public fun ColorPulseSample() {
    Box(
        modifier = Modifier
            .size(16.dp)
            .colorPulse(colors = listOf(Color(0xFFEF4444), Color(0xFFFB923C), Color(0xFFEF4444))),
    )
}

@Composable public fun ShakeSample() {
    var trigger by remember { mutableIntStateOf(0) }
    Box(Modifier.size(96.dp).shake(trigger = trigger).background(Color(0xFFEF4444)))
    @Suppress("UNUSED_EXPRESSION") trigger
}

@Composable public fun GlassmorphismSample() {
    Box(
        modifier = Modifier
            .size(width = 240.dp, height = 100.dp)
            .clip(RoundedCornerShape(20.dp))
            .glassmorphism(blurRadius = 24.dp, tint = Color.White.copy(alpha = 0.18f)),
    )
}

@Composable public fun BounceOnAppearSample() {
    Box(Modifier.size(72.dp).bounceOnAppear().background(Color(0xFF1976D2), CircleShape))
}

@Composable public fun RevealOnScrollSample() {
    Box(Modifier.fillMaxWidth().height(64.dp).revealOnScroll().background(Color(0xFF6366F1)))
}

@Composable public fun MarqueeSample() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color(0xFF111827))
            .marquee(),
    )
}

@Composable public fun SwipeToDismissSample() {
    Box(
        Modifier.fillMaxWidth().height(56.dp).swipeToDismiss(onDismiss = {}).background(Color(0xFF1976D2)),
    )
}

@Composable public fun MagneticSample() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .magnetic(anchors = listOf((-120).dp, 0.dp, 120.dp))
            .background(Color(0xFF6366F1), CircleShape),
    )
}

@Composable public fun PinchZoomSample() {
    Box(Modifier.size(240.dp).pinchZoom().background(Color(0xFF22D3EE)))
}

@Composable public fun DragToReorderSample() {
    val state = rememberReorderableState(initial = listOf("a", "b", "c"))
    Column {
        state.items.forEach { item ->
            Box(
                Modifier.fillMaxWidth().height(48.dp).dragToReorder(state, item).background(Color(0xFF21C386)),
            )
        }
    }
}

@Composable public fun InteractiveCardSample() {
    Box(
        Modifier
            .size(140.dp)
            .interactiveCard()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF6366F1)),
    )
}

@Composable public fun LoadingPlaceholderSample() {
    Box(Modifier.size(width = 240.dp, height = 80.dp).loadingPlaceholder(RoundedCornerShape(12.dp)))
}

