/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.shimmer

import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
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
class ShimmerTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun factory_rejects_degenerate_args() {
        val singleColor = runCatching {
            Modifier.shimmer(colors = listOf(Color.Red))
        }.exceptionOrNull()
        val zeroDuration = runCatching {
            Modifier.shimmer(durationMillis = 0)
        }.exceptionOrNull()
        assertThat(singleColor).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(zeroDuration).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun parameter_change_keeps_the_same_node_attached() {
        var duration by mutableIntStateOf(800)
        composeRule.setContent {
            Box(
                modifier = Modifier
                    .size(120.dp, 24.dp)
                    .testTag("placeholder")
                    .background(Color.LightGray)
                    .shimmer(durationMillis = duration),
            )
        }
        composeRule.onNodeWithTag("placeholder").assertExists()
        composeRule.runOnIdle { duration = 2_000 }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("placeholder").assertExists()
    }

    @Test
    fun repeat_mode_reverse_renders_without_crashing() {
        composeRule.setContent {
            Box(
                modifier = Modifier
                    .size(120.dp, 24.dp)
                    .background(Color.LightGray)
                    .shimmer(repeatMode = RepeatMode.Reverse),
            )
        }
        composeRule.waitForIdle()
    }
}
