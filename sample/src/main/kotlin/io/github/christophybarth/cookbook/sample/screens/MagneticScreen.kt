/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.magnetic.magnetic

@Composable
internal fun MagneticScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            "Drag the dot horizontally. Anchors are clustered on the left (-120 to -20 dp in 20 dp steps), then 0 and +120 dp. " +
                "Notice how the dot snaps tightly between the close-spaced anchors and travels freely across the gap to +120.",
            style = MaterialTheme.typography.bodySmall,
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .magnetic(
                    anchors = listOf((-120).dp, (-100).dp, (-80).dp, (-60).dp, (-40).dp, (-20).dp, 0.dp, 120.dp),
                    snapThreshold = 40.dp,
                )
                .background(Color(0xFF6366F1), CircleShape),
        )
    }
}
