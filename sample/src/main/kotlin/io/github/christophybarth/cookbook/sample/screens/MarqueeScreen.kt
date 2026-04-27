/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.marquee.MarqueeDirection
import io.github.christophybarth.cookbook.marquee.marquee

@Composable
internal fun MarqueeScreen() {
    val darkBg = Color(0xFF111827)
    val indigoBg = Color(0xFF6366F1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Default fade: alpha mask. The DstIn fade now lands on an offscreen layer thanks to the
        // graphicsLayer chained inside the modifier, so edges go genuinely transparent and the
        // dark background drawn behind bleeds through.
        // Note the modifier order: padding is AFTER marquee so the marquee wraps the padded
        // content. That makes the fade rect span the full coloured row height instead of only
        // the inner text height.
        Text(
            text = "Now playing: \"Modifier nodes are the future of Compose APIs\" by The Cookbook",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .background(darkBg)
                .marquee(direction = MarqueeDirection.RightToLeft, fadeEdges = 16.dp)
                .padding(vertical = 8.dp),
        )
        // Explicit fadeColor: paints a coloured gradient on top of the text. Useful when you
        // want the fade to match a known surface even if the layer order would not allow the
        // alpha mask to bleed through.
        Text(
            text = "Slow ticker LeftToRight ::: Slow ticker LeftToRight ::: Slow ticker LeftToRight",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .background(indigoBg)
                .marquee(
                    direction = MarqueeDirection.LeftToRight,
                    velocityDpPerSec = 30.dp,
                    fadeEdges = 30.dp,
                    fadeColor = darkBg,
                )
                .padding(vertical = 8.dp),
        )
    }
}
