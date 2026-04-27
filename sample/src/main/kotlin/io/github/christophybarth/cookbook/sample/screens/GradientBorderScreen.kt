/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.gradientborder.gradientBorder

@Composable
internal fun GradientBorderScreen() {
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
}
