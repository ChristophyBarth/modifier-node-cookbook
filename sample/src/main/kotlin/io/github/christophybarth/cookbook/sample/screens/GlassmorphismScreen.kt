/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.glassmorphism.glassmorphism

@Composable
internal fun GlassmorphismScreen() {
    // The same gradient is drawn twice: once as the visible background and once inside the glass
    // card so glassmorphism's RenderEffect has real pixels to blur (simulating backdrop blur).
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
}
