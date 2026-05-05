/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.christophybarth.cookbook.glass.GlassState
import io.github.christophybarth.cookbook.glass.glass
import io.github.christophybarth.cookbook.glass.glassSource
import io.github.christophybarth.cookbook.glass.rememberGlassState

private val GlassPages = listOf(
    "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1080&q=80",
    "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=1080&q=80",
    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=1080&q=80",
)

@Composable
internal fun GlassScreen() {
    val state = rememberGlassState()
    val pager = rememberPagerState(pageCount = { GlassPages.size })
    var aberration by remember { mutableFloatStateOf(24f) }
    var hueShift by remember { mutableFloatStateOf(0f) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassHero(state = state, pager = pager, aberration = aberration.dp, hueShift = hueShift)
        GlassControls(
            aberration = aberration,
            onAberrationChange = { aberration = it },
            hueShift = hueShift,
            onHueShiftChange = { hueShift = it },
        )
    }
}

@Composable
private fun GlassHero(
    state: GlassState,
    pager: androidx.compose.foundation.pager.PagerState,
    aberration: Dp,
    hueShift: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxSize().glassSource(state),
        ) { i ->
            AsyncImage(
                model = GlassPages[i],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(Color(0xFFE5E7EB)),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ClearGlassCaption(
                state = state,
                currentPage = pager.currentPage,
                chromaticAberration = aberration,
                hueShift = hueShift,
            )
            FrostedGlassCaption(state = state, currentPage = pager.currentPage)
            GlassPageDots(count = GlassPages.size, currentIndex = pager.currentPage)
        }
    }
}

@Composable
private fun GlassControls(
    aberration: Float,
    onAberrationChange: (Float) -> Unit,
    hueShift: Float,
    onHueShiftChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Clear glass controls", style = MaterialTheme.typography.titleSmall)
        GlassSlider(
            label = "Rainbow · ${aberration.toInt()} dp",
            value = aberration,
            onValueChange = onAberrationChange,
            valueRange = 0f..64f,
        )
        GlassSlider(
            label = "Hue shift · ${(hueShift * 360).toInt()}°",
            value = hueShift,
            onValueChange = onHueShiftChange,
            valueRange = 0f..1f,
        )
    }
}

private val PanelShape = RoundedCornerShape(14.dp)

/** Clear glass: barely-there tint + specular border + sheen + rainbow rim. */
@Composable
private fun ClearGlassCaption(
    state: GlassState,
    currentPage: Int,
    chromaticAberration: Dp,
    hueShift: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .glass(
                state = state,
                chromaticAberration = chromaticAberration,
                hueShift = hueShift,
                shape = PanelShape,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "Clear glass · page ${currentPage + 1}",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Frosted glass: saturated luminous backdrop + white tint + sheen + specular border. No rainbow. */
@Composable
private fun FrostedGlassCaption(state: GlassState, currentPage: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .glass(
                state = state,
                blurRadius = 24.dp,
                saturation = 1.6f,
                tint = Color.White.copy(alpha = 0.15f),
                sheenAlpha = 0.18f,
                borderAlpha = 0.45f,
                shape = PanelShape,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "Frosted glass · page ${currentPage + 1}",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun GlassSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun GlassPageDots(count: Int, currentIndex: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(
                        color = if (index == currentIndex) Color.White
                        else Color.White.copy(alpha = 0.5f),
                        shape = CircleShape,
                    ),
            )
        }
    }
}
