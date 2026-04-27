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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.revealonscroll.revealOnScroll

@Composable
internal fun RevealOnScrollScreen() {
    val scroll = rememberScrollState()
    Box(
        modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFF1F5F9)),
    ) {
        Column(modifier = Modifier.verticalScroll(scroll).padding(16.dp)) {
            Text("Scroll down…", style = MaterialTheme.typography.bodyLarge)
            repeat(8) { Box(Modifier.height(40.dp)) }
            repeat(6) { i ->
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                        .height(48.dp)
                        .revealOnScroll()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF6366F1)),
                    contentAlignment = Alignment.Center,
                ) { Text("Reveal #$i", color = Color.White) }
            }
        }
    }
}
