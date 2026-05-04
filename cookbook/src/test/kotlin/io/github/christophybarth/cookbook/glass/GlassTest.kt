/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GlassTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_negative_blur() {
        var caught: Throwable? = null
        composeRule.setContent {
            val state = rememberGlassState()
            caught = runCatching {
                Modifier.glass(state, blurRadius = (-1).dp)
            }.exceptionOrNull()
        }
        composeRule.waitForIdle()
        assertThat(caught).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun rejects_negative_saturation() {
        var caught: Throwable? = null
        composeRule.setContent {
            val state = rememberGlassState()
            caught = runCatching {
                Modifier.glass(state, saturation = -0.1f)
            }.exceptionOrNull()
        }
        composeRule.waitForIdle()
        assertThat(caught).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun rejects_negative_chromatic_aberration() {
        var caught: Throwable? = null
        composeRule.setContent {
            val state = rememberGlassState()
            caught = runCatching {
                Modifier.glass(state, chromaticAberration = (-1).dp)
            }.exceptionOrNull()
        }
        composeRule.waitForIdle()
        assertThat(caught).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun renders_source_and_panel_without_crashing() {
        composeRule.setContent {
            val state = rememberGlassState()
            Box(modifier = Modifier.size(160.dp).testTag("root")) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .glassSource(state)
                        .background(Color(0xFF1976D2)),
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .testTag("glass")
                        .glass(state, blurRadius = 12.dp, saturation = 1.4f),
                )
            }
        }
        composeRule.onNodeWithTag("root").assertExists()
        composeRule.onNodeWithTag("glass").assertExists()
    }
}
