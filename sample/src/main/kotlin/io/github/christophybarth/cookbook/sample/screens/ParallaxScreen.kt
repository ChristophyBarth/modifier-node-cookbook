/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.parallax.parallax

@Composable
internal fun ParallaxScreen() {
    val scroll = rememberScrollState()
    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        // Background hero that parallax-shifts as the foreground scrolls.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .parallax(scroll, factor = 0.5f)
                .background(Color(0xFF6750A4))
                .align(Alignment.TopCenter),
        )
        Column(modifier = Modifier.verticalScroll(scroll).padding(top = 80.dp)) {
            repeat(20) { i ->
                Text(
                    text = "Foreground row #$i",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp),
                )
            }
        }
    }
}
