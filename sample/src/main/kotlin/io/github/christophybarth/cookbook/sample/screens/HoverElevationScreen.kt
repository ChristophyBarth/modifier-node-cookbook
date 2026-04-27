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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.hoverelevation.hoverElevation

@Composable
internal fun HoverElevationScreen() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(96.dp).hoverElevation(restingDp = 2.dp, hoveredDp = 16.dp).background(Color.White))
        Box(modifier = Modifier.size(96.dp).hoverElevation(restingDp = 4.dp, hoveredDp = 24.dp).background(Color.White))
        Text("Hover with mouse", style = MaterialTheme.typography.bodySmall)
    }
}
