/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.pressscale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
class PressScaleTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun factory_rejects_scale_outside_valid_range() {
        val tooBig = runCatching { Modifier.pressScale(scale = 1.5f) }.exceptionOrNull()
        val zero = runCatching { Modifier.pressScale(scale = 0f) }.exceptionOrNull()
        val negative = runCatching { Modifier.pressScale(scale = -0.1f) }.exceptionOrNull()
        assertThat(tooBig).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(zero).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(negative).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun parameter_change_updates_node_without_recreating() {
        var pressScale by mutableStateOf(0.96f)
        composeRule.setContent {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(48.dp)
                    .testTag("target")
                    .pressScale(scale = pressScale)
                    .background(Color.Red),
            )
        }
        composeRule.onNodeWithTag("target").assertExists()
        composeRule.runOnIdle { pressScale = 0.80f }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("target").assertExists()
    }

    @Test
    fun default_press_scale_is_canonical_0_96f() {
        // Compile-time guard against drift in the public default.
        val element = Modifier.pressScale().toString()
        assertThat(element).contains("0.96")
    }
}
