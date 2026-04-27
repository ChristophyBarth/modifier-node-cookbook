/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
 * Catalog screen: one entry per modifier in the cookbook. Each entry hosts a self-contained demo
 * composable that exercises the modifier on representative content.
 */
@Composable
internal fun CookbookCatalog() {
    val entries = remember { catalogEntries() }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
    ) {
        items(entries, key = { it.id }) { entry ->
            Text(text = entry.title, style = MaterialTheme.typography.titleMedium)
            entry.demo()
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }
    }
}

internal data class CatalogEntry(
    val id: String,
    val title: String,
    val demo: @Composable () -> Unit,
)

internal fun catalogEntries(): List<CatalogEntry> = listOf(
    // v0.1
    CatalogEntry("pressScale", "Modifier.pressScale") { PressScaleScreen() },
    CatalogEntry("shimmer", "Modifier.shimmer") { ShimmerScreen() },
    CatalogEntry("skeleton", "Modifier.skeleton") { SkeletonScreen() },
    CatalogEntry("hoverElevation", "Modifier.hoverElevation") { HoverElevationScreen() },
    CatalogEntry("fadeScrollEdges", "Modifier.fadeScrollEdges") { FadeScrollEdgesScreen() },
    CatalogEntry("parallax", "Modifier.parallax") { ParallaxScreen() },
    CatalogEntry("tilt", "Modifier.tilt") { TiltScreen() },
    CatalogEntry("longPressProgress", "Modifier.longPressProgress") { LongPressProgressScreen() },
    // v0.2 standalone
    CatalogEntry("dropShadow", "Modifier.dropShadow") { DropShadowScreen() },
    CatalogEntry("gradientBorder", "Modifier.gradientBorder") { GradientBorderScreen() },
    CatalogEntry("colorPulse", "Modifier.colorPulse") { ColorPulseScreen() },
    CatalogEntry("shake", "Modifier.shake") { ShakeScreen() },
    CatalogEntry("glassmorphism", "Modifier.glassmorphism") { GlassmorphismScreen() },
    CatalogEntry("bounceOnAppear", "Modifier.bounceOnAppear") { BounceOnAppearScreen() },
    CatalogEntry("revealOnScroll", "Modifier.revealOnScroll") { RevealOnScrollScreen() },
    CatalogEntry("marquee", "Modifier.marquee") { MarqueeScreen() },
    CatalogEntry("swipeToDismiss", "Modifier.swipeToDismiss") { SwipeToDismissScreen() },
    CatalogEntry("magnetic", "Modifier.magnetic") { MagneticScreen() },
    CatalogEntry("pinchZoom", "Modifier.pinchZoom") { PinchZoomScreen() },
    CatalogEntry("dragToReorder", "Modifier.dragToReorder") { DragToReorderScreen() },
    // v0.2 composites
    CatalogEntry("interactiveCard", "Modifier.interactiveCard (composite)") { InteractiveCardScreen() },
    CatalogEntry("loadingPlaceholder", "Modifier.loadingPlaceholder (composite)") { LoadingPlaceholderScreen() },
)
