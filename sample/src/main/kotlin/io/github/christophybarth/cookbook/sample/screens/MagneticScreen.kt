/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.magnetic.magnetic

@Composable
internal fun MagneticScreen() {
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
}
