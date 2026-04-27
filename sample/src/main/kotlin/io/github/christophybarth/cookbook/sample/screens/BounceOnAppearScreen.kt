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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.bounceonappear.bounceOnAppear

@Composable
internal fun BounceOnAppearScreen() {
    var version by remember { mutableIntStateOf(0) }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(72.dp)) {
            // key(version) forces a full remount when version changes, which detaches then
            // re-attaches the bounceOnAppear node, replaying the spring animation.
            key(version) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .bounceOnAppear()
                        .background(Color(0xFF1976D2), CircleShape),
                )
            }
        }
        Button(onClick = { version++ }) { Text("Replay") }
    }
}
