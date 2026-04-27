/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.colorpulse.colorPulse

@Composable
internal fun ColorPulseScreen() {
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
}
