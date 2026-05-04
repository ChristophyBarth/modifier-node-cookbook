/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Detail screen: a fixed-size hero canvas centered on a tuned surface, with the modifier's name
 * in the app bar. The hero canvas is the recording target — same dimensions across all 22 demos
 * so the resulting GIF set looks like a coherent collection. Below the hero sits a description
 * and a styled code snippet for quick reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DemoDetail(entry: CatalogEntry, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            HeroCanvas(background = entry.background) {
                entry.demo()
            }
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
            CodeBlock(
                code = entry.code,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

/**
 * Fixed-size container that frames a single demo for filming. Same width/height/padding for every
 * modifier so all GIFs share identical canvas dimensions. The surface tint is chosen per modifier
 * via [HeroBackground] — shimmer/dropShadow read better on Light, gradient borders on Dark, and
 * demos that paint their own backdrop (glass, glassmorphism, marquee) opt out via [HeroBackground.Own].
 */
@Composable
private fun HeroCanvas(
    background: HeroBackground,
    content: @Composable () -> Unit,
) {
    val color = when (background) {
        HeroBackground.Neutral -> MaterialTheme.colorScheme.surfaceVariant
        HeroBackground.Light -> Color(0xFFFAFAFA)
        HeroBackground.Dark -> Color(0xFF111827)
        HeroBackground.Own -> Color.Transparent
    }
    if (background == HeroBackground.Own) {
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = HERO_HEIGHT_DP.dp),
            contentAlignment = Alignment.Center,
        ) { content() }
    } else {
        Surface(
            modifier = HeroSize,
            color = color,
            shape = RoundedCornerShape(16.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) { content() }
        }
    }
}

/**
 * Styled, copyable snippet pane. Monospace text on a dark surface so it reads as code at a glance,
 * scrolls horizontally for long lines (no wrapping in the middle of a `.modifierCall(...)` chain),
 * and is wrapped in [SelectionContainer] so users can select-and-copy.
 */
@Composable
private fun CodeBlock(code: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Usage",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CodeSurface,
            shape = RoundedCornerShape(12.dp),
        ) {
            SelectionContainer {
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Text(
                        text = code,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = CodeForeground,
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    )
                }
            }
        }
    }
}

private val HeroSize: Modifier = Modifier
    .fillMaxWidth()
    .height(HERO_HEIGHT_DP.dp)

private const val HERO_HEIGHT_DP = 320

private val CodeSurface = Color(0xFF1E1E2E)
private val CodeForeground = Color(0xFFE2E8F0)
