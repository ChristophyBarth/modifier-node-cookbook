/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.pressscale.pressScale

@Composable
internal fun PressScaleScreen() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PressTile(scale = 0.96f, color = Color(0xFF6750A4))
        PressTile(scale = 0.88f, color = Color(0xFF21C386))
        PressTile(scale = 0.80f, color = Color(0xFFFFB300))
        Text("Tap a tile", style = MaterialTheme.typography.bodySmall)
    }
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
