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
import io.github.christophybarth.cookbook.sample.screens.GlassScreen
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

    /** Skip the surface entirely — the demo provides its own background (glass, marquee). */
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
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .pressScale(scale = 0.88f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF6750A4))
                    .clickable { },
            )
        """.trimIndent(),
    ) { PressScaleScreen() },
    CatalogEntry(
        id = "shimmer",
        title = "Modifier.shimmer",
        description = "Sweeps a translucent gradient over content. Loading state in motion.",
        background = HeroBackground.Light,
        code = """
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFCCCCCC))
                    .shimmer(),
            )
        """.trimIndent(),
    ) { ShimmerScreen() },
    CatalogEntry(
        id = "skeleton",
        title = "Modifier.skeleton",
        description = "Opaque placeholder shape. Composes with shimmer.",
        background = HeroBackground.Light,
        code = """
            // shimmer before skeleton: skeleton does not call drawContent().
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 80.dp)
                    .shimmer()
                    .skeleton(RoundedCornerShape(12.dp), color = Color(0xFFCCCCCC)),
            )
        """.trimIndent(),
    ) { SkeletonScreen() },
    CatalogEntry(
        id = "hoverElevation",
        title = "Modifier.hoverElevation",
        description = "Animates shadow elevation on mouse / stylus hover.",
        background = HeroBackground.Light,
        code = """
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .hoverElevation(restingDp = 2.dp, hoveredDp = 16.dp)
                    .background(Color.White),
            )
        """.trimIndent(),
    ) { HoverElevationScreen() },
    CatalogEntry(
        id = "fadeScrollEdges",
        title = "Modifier.fadeScrollEdges",
        description = "Fades leading & trailing edges of a scrollable based on position.",
        background = HeroBackground.Own,
        code = """
            val scroll = rememberScrollState()
            Box(modifier = Modifier.height(220.dp).fadeScrollEdges(scroll)) {
                Column(modifier = Modifier.verticalScroll(scroll)) {
                    repeat(40) { Text("Row #${'$'}it") }
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
            Box {
                // Background shifts at half the foreground's scroll rate.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .parallax(scroll, factor = 0.5f)
                        .background(Color(0xFF6750A4)),
                )
                Column(modifier = Modifier.verticalScroll(scroll)) {
                    repeat(20) { Text("Row #${'$'}it") }
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
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .tilt(maxAngleDeg = 12f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF21C386)),
            )
        """.trimIndent(),
    ) { TiltScreen() },
    CatalogEntry(
        id = "longPressProgress",
        title = "Modifier.longPressProgress",
        description = "Radial progress arc while long-press is held.",
        background = HeroBackground.Neutral,
        code = """
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color(0xFFEF4444), CircleShape)
                    .longPressProgress(durationMs = 1_000L) { /* completed */ },
            )
        """.trimIndent(),
    ) { LongPressProgressScreen() },
    CatalogEntry(
        id = "dropShadow",
        title = "Modifier.dropShadow",
        description = "Custom drop shadow untied from Material elevation.",
        background = HeroBackground.Light,
        code = """
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
        """.trimIndent(),
    ) { DropShadowScreen() },
    CatalogEntry(
        id = "gradientBorder",
        title = "Modifier.gradientBorder",
        description = "Animated gradient stroke around the receiver.",
        background = HeroBackground.Dark,
        code = """
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF111827))
                    .gradientBorder(width = 3.dp, shape = RoundedCornerShape(16.dp)),
            )
        """.trimIndent(),
    ) { GradientBorderScreen() },
    CatalogEntry(
        id = "colorPulse",
        title = "Modifier.colorPulse",
        description = "Periodic colour cycling for badges and attention.",
        background = HeroBackground.Neutral,
        code = """
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .colorPulse(
                        colors = listOf(Color(0xFFEF4444), Color(0xFFFB923C), Color(0xFFEF4444)),
                        shape = CircleShape,
                    ),
            )
        """.trimIndent(),
    ) { ColorPulseScreen() },
    CatalogEntry(
        id = "shake",
        title = "Modifier.shake",
        description = "Single-shot horizontal shake driven by a trigger key.",
        background = HeroBackground.Neutral,
        code = """
            var trigger by remember { mutableIntStateOf(0) }
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shake(trigger = trigger)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF4444)),
            )
            Button(onClick = { trigger++ }) { Text("Shake") }
        """.trimIndent(),
    ) { ShakeScreen() },
    CatalogEntry(
        id = "glass",
        title = "Modifier.glass",
        description = "Backdrop sampler that reads as glass. Defaults are clear; raise saturation, sheen and border for frosted.",
        background = HeroBackground.Own,
        code = """
            val state = rememberGlassState()
            val shape = RoundedCornerShape(14.dp)
            Box {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().glassSource(state),
                )
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .glass(state, shape = shape)
                        .padding(16.dp),
                ) { Text("Glass panel", color = Color.White) }
            }
        """.trimIndent(),
    ) { GlassScreen() },
    CatalogEntry(
        id = "bounceOnAppear",
        title = "Modifier.bounceOnAppear",
        description = "Single-shot entry bounce on attach.",
        background = HeroBackground.Neutral,
        code = """
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .bounceOnAppear()
                    .background(Color(0xFF1976D2), CircleShape),
            )
        """.trimIndent(),
    ) { BounceOnAppearScreen() },
    CatalogEntry(
        id = "revealOnScroll",
        title = "Modifier.revealOnScroll",
        description = "Fade + translate in as element enters the viewport.",
        background = HeroBackground.Own,
        code = """
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                repeat(6) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .revealOnScroll()
                            .background(Color(0xFF6366F1)),
                    )
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
            Text(
                text = "Now playing long song title that overflows...",
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .marquee(direction = MarqueeDirection.RightToLeft, fadeEdges = 16.dp),
            )
        """.trimIndent(),
    ) { MarqueeScreen() },
    CatalogEntry(
        id = "swipeToDismiss",
        title = "Modifier.swipeToDismiss",
        description = "Drag past threshold dismisses; springs back otherwise.",
        background = HeroBackground.Neutral,
        code = """
            var rows by remember { mutableStateOf(listOf("Row 1", "Row 2", "Row 3")) }
            rows.forEach { row ->
                // key(row) so each row owns its own swipe node; otherwise Compose reuses
                // the dismissed row's offset for whatever takes its place.
                key(row) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .swipeToDismiss(onDismiss = { rows = rows - row })
                            .background(Color(0xFF1976D2)),
                    ) { Text(row, color = Color.White) }
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
            val anchors = listOf((-120).dp, 0.dp, 120.dp)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .magnetic(anchors = anchors, snapThreshold = 60.dp)
                    .background(Color(0xFF6366F1), CircleShape),
            )
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
                    .pinchZoom(minScale = 1f, maxScale = 5f)
                    .background(Brush.radialGradient(listOf(Color(0xFF22D3EE), Color(0xFFEF4444)))),
            )
        """.trimIndent(),
    ) { PinchZoomScreen() },
    CatalogEntry(
        id = "dragToReorder",
        title = "Modifier.dragToReorder",
        description = "Long-press to lift, drag to reorder; pairs with ReorderableState.",
        background = HeroBackground.Neutral,
        code = """
            val state = rememberReorderableState(initial = listOf("Apples", "Bread", "Coffee"))
            state.items.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .dragToReorder(state, item)
                        .background(Color(0xFF21C386)),
                ) { Text(item, color = Color.White) }
            }
        """.trimIndent(),
    ) { DragToReorderScreen() },
    CatalogEntry(
        id = "interactiveCard",
        title = "Modifier.interactiveCard",
        description = "Composite: pressScale + tilt + hoverElevation.",
        background = HeroBackground.Neutral,
        code = """
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .interactiveCard()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF6366F1))
                    .clickable { },
            )
        """.trimIndent(),
    ) { InteractiveCardScreen() },
    CatalogEntry(
        id = "loadingPlaceholder",
        title = "Modifier.loadingPlaceholder",
        description = "Composite: skeleton + shimmer.",
        background = HeroBackground.Light,
        code = """
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 80.dp)
                    .loadingPlaceholder(RoundedCornerShape(12.dp), color = Color(0xFFCCCCCC)),
            )
        """.trimIndent(),
    ) { LoadingPlaceholderScreen() },
)
