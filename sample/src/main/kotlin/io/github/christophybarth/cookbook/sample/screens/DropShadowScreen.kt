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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.dropshadow.dropShadow

@Composable
internal fun DropShadowScreen() {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
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
        Box(
            modifier = Modifier
                .size(80.dp)
                .dropShadow(
                    color = Color(0xFF6366F1).copy(alpha = 0.45f),
                    blur = 24.dp,
                    offset = DpOffset(0.dp, 8.dp),
                    shape = RoundedCornerShape(16.dp),
                )
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
        )
    }
}
