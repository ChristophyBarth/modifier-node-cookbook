/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.sample.screens.BounceOnAppearScreen
import io.github.christophybarth.cookbook.sample.screens.ColorPulseScreen
import io.github.christophybarth.cookbook.sample.screens.DragToReorderScreen
import io.github.christophybarth.cookbook.sample.screens.DropShadowScreen
import io.github.christophybarth.cookbook.sample.screens.FadeScrollEdgesScreen
import io.github.christophybarth.cookbook.sample.screens.GlassmorphismScreen
import io.github.christophybarth.cookbook.sample.screens.GradientBorderScreen
import io.github.christophybarth.cookbook.sample.screens.HoverElevationScreen
import io.github.christophybarth.cookbook.sample.screens.InteractiveCardScreen
import io.github.christophybarth.cookbook.sample.screens.LoadingPlaceholderScreen
import io.github.christophybarth.cookbook.sample.screens.LongPressProgressScreen
import io.github.christophybarth.cookbook.sample.screens.MagneticScreen
import io.github.christophybarth.cookbook.sample.screens.MarqueeScreen
import io.github.christophybarth.cookbook.sample.screens.ParallaxScreen
import io.github.christophybarth.cookbook.sample.screens.PinchZoomScreen
import io.github.christophybarth.cookbook.sample.screens.PressScaleScreen
import io.github.christophybarth.cookbook.sample.screens.RevealOnScrollScreen
import io.github.christophybarth.cookbook.sample.screens.ShakeScreen
import io.github.christophybarth.cookbook.sample.screens.ShimmerScreen
import io.github.christophybarth.cookbook.sample.screens.SkeletonScreen
import io.github.christophybarth.cookbook.sample.screens.SwipeToDismissScreen
import io.github.christophybarth.cookbook.sample.screens.TiltScreen

/**
 * Menu screen: list of cards, one per modifier. Tapping a card invokes [onSelect] with the entry,
 * which the host then routes to a dedicated [DemoDetail] screen for clean filming and exploration.
 */
@Composable
internal fun CatalogMenu(
    entries: List<CatalogEntry>,
    onSelect: (CatalogEntry) -> Unit,
    listState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(entries, key = { it.id }) { entry ->
            Card(
                onClick = { onSelect(entry) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = entry.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Background style for a demo's hero canvas. Different modifiers read best on different surfaces. */
internal enum class HeroBackground {
    /** MaterialTheme surface variant. Default — works for most colored content. */
    Neutral,
    /** Near-white. Best for shimmer/skeleton/dropShadow where light grey content needs contrast. */
    Light,
    /** Deep slate. Best for gradient borders and color pulses that pop against dark. */
    Dark,
    /** Skip the surface entirely — the demo provides its own background (glassmorphism, marquee). */
    Own,
}

internal data class CatalogEntry(
    val id: String,
    val title: String,
    val description: String,
    val background: HeroBackground,
    /** Hand-copied from `screens/<Name>Screen.kt`. No compile-time sync — update both sides. */
    val code: String,
    val demo: @Composable () -> Unit,
)

@Suppress("LongMethod") // Static registry of demos; size is data, not complexity.
internal fun catalogEntries(): List<CatalogEntry> = listOf(
    CatalogEntry(
        id = "pressScale",
        title = "Modifier.pressScale",
        description = "Scales down on press; springs back on release.",
        background = HeroBackground.Neutral,
        code = """
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PressTile(scale = 0.96f, color = Color(0xFF6750A4))
                PressTile(scale = 0.88f, color = Color(0xFF21C386))
                PressTile(scale = 0.80f, color = Color(0xFFFFB300))
                Text("Tap a tile", style = MaterialTheme.typography.bodySmall)
            }

            @Composable
            private fun PressTile(scale: Float, color: Color) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .pressScale(scale = scale)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color)
                        .clickable { /* visual demo only */ },
                )
            }
        """.trimIndent(),
    ) { PressScaleScreen() },
    CatalogEntry(
        id = "shimmer",
        title = "Modifier.shimmer",
        description = "Sweeps a translucent gradient over content. Loading state in motion.",
        background = HeroBackground.Light,
        code = """
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Card-shaped placeholder
                Box(
                    modifier = Modifier
                        .size(width = 240.dp, height = 120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFCCCCCC))
                        .shimmer(),
                )
                // Headline placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFCCCCCC))
                        .shimmer(),
                )
                // Body line placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFCCCCCC))
                        .shimmer(),
                )
            }
        """.trimIndent(),
    ) { ShimmerScreen() },
    CatalogEntry(
        id = "skeleton",
        title = "Modifier.skeleton",
        description = "Opaque placeholder shape. Composes with shimmer.",
        background = HeroBackground.Light,
        code = """
            val fill = Color(0xFFCCCCCC)
            // shimmer before skeleton: skeleton does not drawContent(), so anything after it is dead.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(width = 240.dp, height = 80.dp).shimmer().skeleton(RoundedCornerShape(12.dp), color = fill))
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).shimmer().skeleton(RoundedCornerShape(8.dp), color = fill))
                Box(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp).shimmer().skeleton(RoundedCornerShape(8.dp), color = fill))
            }
        """.trimIndent(),
    ) { SkeletonScreen() },
    CatalogEntry(
        id = "hoverElevation",
        title = "Modifier.hoverElevation",
        description = "Animates shadow elevation on mouse / stylus hover.",
        background = HeroBackground.Light,
        code = """
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(96.dp).hoverElevation(restingDp = 2.dp, hoveredDp = 16.dp).background(Color.White))
                Box(modifier = Modifier.size(96.dp).hoverElevation(restingDp = 4.dp, hoveredDp = 24.dp).background(Color.White))
                Text("Hover with mouse", style = MaterialTheme.typography.bodySmall)
            }
        """.trimIndent(),
    ) { HoverElevationScreen() },
    CatalogEntry(
        id = "fadeScrollEdges",
        title = "Modifier.fadeScrollEdges",
        description = "Fades leading & trailing edges of a scrollable based on position.",
        background = HeroBackground.Own,
        code = """
            val scroll = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFFFAFAFA))
                    .fadeScrollEdges(scroll),
            ) {
                Column(modifier = Modifier.verticalScroll(scroll).padding(16.dp)) {
                    repeat(40) { i ->
                        Text(
                            text = "Row #${'$'}i, scrolls under fading edges",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }
            }
        """.trimIndent(),
    ) { FadeScrollEdgesScreen() },
    CatalogEntry(
        id = "parallax",
        title = "Modifier.parallax",
        description = "Translates a child relative to a parent scroll container.",
        background = HeroBackground.Own,
        code = """
            val scroll = rememberScrollState()
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                // Background hero that parallax-shifts as the foreground scrolls.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .parallax(scroll, factor = 0.5f)
                        .background(Color(0xFF6750A4))
                        .align(Alignment.TopCenter),
                )
                Column(modifier = Modifier.verticalScroll(scroll).padding(top = 80.dp)) {
                    repeat(20) { i ->
                        Text(
                            text = "Foreground row #${'$'}i",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp),
                        )
                    }
                }
            }
        """.trimIndent(),
    ) { ParallaxScreen() },
    CatalogEntry(
        id = "tilt",
        title = "Modifier.tilt",
        description = "3D card-tilt that follows pointer position.",
        background = HeroBackground.Neutral,
        code = """
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .tilt(maxAngleDeg = 12f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF21C386), Color(0xFF1976D2)))),
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .tilt(maxAngleDeg = 20f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFB300)),
                )
            }
        """.trimIndent(),
    ) { TiltScreen() },
    CatalogEntry(
        id = "longPressProgress",
        title = "Modifier.longPressProgress",
        description = "Radial progress arc while long-press is held.",
        background = HeroBackground.Neutral,
        code = """
            var fired by remember { mutableIntStateOf(0) }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Color(0xFFEF4444), CircleShape)
                        .longPressProgress(durationMs = 1_000L) { fired++ },
                )
                Text("Hold the circle (${'$'}fired completions)", style = MaterialTheme.typography.bodyMedium)
            }
        """.trimIndent(),
    ) { LongPressProgressScreen() },
    CatalogEntry(
        id = "dropShadow",
        title = "Modifier.dropShadow",
        description = "Custom drop shadow untied from Material elevation.",
        background = HeroBackground.Light,
        code = """
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .dropShadow(
                            color = Color.Black.copy(alpha = 0.18f),
                            blur = 12.dp,
                            offset = DpOffset(0.dp, 4.dp),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .dropShadow(
                            color = Color(0xFF6366F1).copy(alpha = 0.45f),
                            blur = 24.dp,
                            offset = DpOffset(0.dp, 8.dp),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                )
            }
        """.trimIndent(),
    ) { DropShadowScreen() },
    CatalogEntry(
        id = "gradientBorder",
        title = "Modifier.gradientBorder",
        description = "Animated gradient stroke around the receiver.",
        background = HeroBackground.Dark,
        code = """
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF111827))
                        .gradientBorder(width = 3.dp, shape = RoundedCornerShape(16.dp)),
                )
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF111827))
                        .gradientBorder(
                            colors = listOf(Color(0xFF22D3EE), Color(0xFF8B5CF6), Color(0xFF22D3EE)),
                            width = 6.dp,
                            durationMillis = 5_000,
                            shape = RoundedCornerShape(16.dp),
                        ),
                )
            }
        """.trimIndent(),
    ) { GradientBorderScreen() },
    CatalogEntry(
        id = "colorPulse",
        title = "Modifier.colorPulse",
        description = "Periodic colour cycling for badges and attention.",
        background = HeroBackground.Neutral,
        code = """
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .colorPulse(
                            colors = listOf(Color(0xFFEF4444), Color(0xFFFB923C), Color(0xFFEF4444)),
                            shape = CircleShape,
                        ),
                )
                Text("3 unread", style = MaterialTheme.typography.bodyMedium)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .colorPulse(
                            colors = listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFF10B981)),
                            durationMs = 2_400,
                            shape = CircleShape,
                        ),
                )
                Text("Live", style = MaterialTheme.typography.bodyMedium)
            }
        """.trimIndent(),
    ) { ColorPulseScreen() },
    CatalogEntry(
        id = "shake",
        title = "Modifier.shake",
        description = "Single-shot horizontal shake driven by a trigger key.",
        background = HeroBackground.Neutral,
        code = """
            var trigger by remember { mutableIntStateOf(0) }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .shake(trigger = trigger, oscillations = 10, durationMs = 5000)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEF4444)),
                )
                Button(onClick = { trigger++ }) { Text("Trigger error") }
            }
        """.trimIndent(),
    ) { ShakeScreen() },
    CatalogEntry(
        id = "glassmorphism",
        title = "Modifier.glassmorphism",
        description = "Frosted glass; real blur on API 31+, tint fallback below.",
        background = HeroBackground.Own,
        code = """
            // The same gradient is drawn twice: once as the visible background and once inside
            // the glass card so glassmorphism's RenderEffect has real pixels to blur (simulating
            // backdrop blur).
            val gradient = Brush.linearGradient(
                listOf(Color(0xFFEF4444), Color(0xFFFBBF24), Color(0xFF22D3EE), Color(0xFF8B5CF6)),
            )
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).background(gradient),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 240.dp, height = 100.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    // This inner box provides gradient pixels for glassmorphism to blur.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .glassmorphism(blurRadius = 20.dp, tint = Color.White.copy(alpha = 0.30f))
                            .background(gradient),
                    )
                    Text("Frosted glass", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
        """.trimIndent(),
    ) { GlassmorphismScreen() },
    CatalogEntry(
        id = "bounceOnAppear",
        title = "Modifier.bounceOnAppear",
        description = "Single-shot entry bounce on attach.",
        background = HeroBackground.Neutral,
        code = """
            var version by remember { mutableIntStateOf(0) }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(72.dp)) {
                    // key(version) forces a full remount when version changes, which detaches
                    // then re-attaches the bounceOnAppear node, replaying the spring animation.
                    key(version) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .bounceOnAppear()
                                .background(Color(0xFF1976D2), CircleShape),
                        )
                    }
                }
                Button(onClick = { version++ }) { Text("Replay") }
            }
        """.trimIndent(),
    ) { BounceOnAppearScreen() },
    CatalogEntry(
        id = "revealOnScroll",
        title = "Modifier.revealOnScroll",
        description = "Fade + translate in as element enters the viewport.",
        background = HeroBackground.Own,
        code = """
            val scroll = rememberScrollState()
            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFF1F5F9)),
            ) {
                Column(modifier = Modifier.verticalScroll(scroll).padding(16.dp)) {
                    Text("Scroll down…", style = MaterialTheme.typography.bodyLarge)
                    repeat(8) { Box(Modifier.height(40.dp)) }
                    repeat(6) { i ->
                        Box(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                                .height(48.dp)
                                .revealOnScroll()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF6366F1)),
                            contentAlignment = Alignment.Center,
                        ) { Text("Reveal #${'$'}i", color = Color.White) }
                    }
                }
            }
        """.trimIndent(),
    ) { RevealOnScrollScreen() },
    CatalogEntry(
        id = "marquee",
        title = "Modifier.marquee",
        description = "Auto-scroll on overflow with configurable velocity.",
        background = HeroBackground.Own,
        code = """
            val darkBg = Color(0xFF111827)
            val indigoBg = Color(0xFF6366F1)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Default fade: alpha mask. The DstIn fade now lands on an offscreen layer
                // thanks to the graphicsLayer chained inside the modifier, so edges go
                // genuinely transparent and the dark background drawn behind bleeds through.
                // Note the modifier order: padding is AFTER marquee so the marquee wraps
                // the padded content. That makes the fade rect span the full coloured row
                // height instead of only the inner text height.
                Text(
                    text = "Now playing: …",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(darkBg)
                        .marquee(direction = MarqueeDirection.RightToLeft, fadeEdges = 16.dp)
                        .padding(vertical = 8.dp),
                )
                // Explicit fadeColor: paints a coloured gradient on top of the text. Useful
                // when you want the fade to match a known surface even if the layer order
                // would not allow the alpha mask to bleed through.
                Text(
                    text = "Slow ticker LeftToRight ::: …",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(indigoBg)
                        .marquee(
                            direction = MarqueeDirection.LeftToRight,
                            velocityDpPerSec = 30.dp,
                            fadeEdges = 30.dp,
                            fadeColor = darkBg,
                        )
                        .padding(vertical = 8.dp),
                )
            }
        """.trimIndent(),
    ) { MarqueeScreen() },
    CatalogEntry(
        id = "swipeToDismiss",
        title = "Modifier.swipeToDismiss",
        description = "Drag past threshold dismisses; springs back otherwise.",
        background = HeroBackground.Neutral,
        code = """
            var rows by remember { mutableStateOf(listOf("Row 1", "Row 2", "Row 3", "Row 4")) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rows.forEach { row ->
                    // key(row) ensures each row owns its own SwipeToDismissNode. Without it,
                    // removing Row 1 causes Compose to reuse its node (fully-swiped offset)
                    // for Row 2.
                    key(row) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .swipeToDismiss(onDismiss = { rows = rows - row })
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1976D2))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(row, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        """.trimIndent(),
    ) { SwipeToDismissScreen() },
    CatalogEntry(
        id = "magnetic",
        title = "Modifier.magnetic",
        description = "Drag snaps to the nearest anchor on release.",
        background = HeroBackground.Neutral,
        code = """
            val anchors = listOf((-120).dp, (-60).dp, 0.dp, 60.dp, 120.dp)
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(4.dp)
                        .background(Color(0xFFCBD5E1), CircleShape),
                )
                anchors.forEach { offset ->
                    Box(
                        modifier = Modifier
                            .offset(x = offset)
                            .size(12.dp)
                            .background(Color(0xFF475569), CircleShape),
                    )
                }
                // snapThreshold > half the spacing so every release point is in range of an anchor.
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .magnetic(anchors = anchors, snapThreshold = 50.dp)
                        .background(Color(0xFF6366F1), CircleShape),
                )
            }
        """.trimIndent(),
    ) { MagneticScreen() },
    CatalogEntry(
        id = "pinchZoom",
        title = "Modifier.pinchZoom",
        description = "Two-finger zoom with pan; clamped scale.",
        background = HeroBackground.Own,
        code = """
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .pinchZoom(minScale = 1f, maxScale = 5f)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF22D3EE), Color(0xFF8B5CF6), Color(0xFFEF4444))),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("Pinch to zoom", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        """.trimIndent(),
    ) { PinchZoomScreen() },
    CatalogEntry(
        id = "dragToReorder",
        title = "Modifier.dragToReorder",
        description = "Long-press to lift, drag to reorder; pairs with ReorderableState.",
        background = HeroBackground.Neutral,
        code = """
            val state = rememberReorderableState(initial = listOf("Apples", "Bread", "Coffee"))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                state.items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .dragToReorder(state, item)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF21C386))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(item, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        """.trimIndent(),
    ) { DragToReorderScreen() },
    CatalogEntry(
        id = "interactiveCard",
        title = "Modifier.interactiveCard",
        description = "Composite: pressScale + tilt + hoverElevation.",
        background = HeroBackground.Neutral,
        code = """
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .interactiveCard()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFFEC4899))))
                        .clickable { /* tap */ },
                )
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .interactiveCard(tiltAngleDeg = 14f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF22D3EE), Color(0xFF8B5CF6))))
                        .clickable { /* tap */ },
                )
            }
        """.trimIndent(),
    ) { InteractiveCardScreen() },
    CatalogEntry(
        id = "loadingPlaceholder",
        title = "Modifier.loadingPlaceholder",
        description = "Composite: skeleton + shimmer.",
        background = HeroBackground.Light,
        code = """
            val fill = Color(0xFFCCCCCC)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(width = 240.dp, height = 80.dp).loadingPlaceholder(RoundedCornerShape(12.dp), color = fill))
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).loadingPlaceholder(RoundedCornerShape(8.dp), color = fill))
                Box(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp).loadingPlaceholder(RoundedCornerShape(8.dp), color = fill))
                Box(modifier = Modifier.size(48.dp).loadingPlaceholder(CircleShape, color = fill))
            }
        """.trimIndent(),
    ) { LoadingPlaceholderScreen() },
)
