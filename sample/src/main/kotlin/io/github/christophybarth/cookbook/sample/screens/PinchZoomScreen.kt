/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import io.github.christophybarth.cookbook.pinchzoom.pinchZoom

@Composable
internal fun PinchZoomScreen() {
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
}
