/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.colorpulse

import androidx.compose.foundation.layout.Box
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
class ColorPulseTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_degenerate_args() {
        val one = runCatching { Modifier.colorPulse(colors = listOf(Color.Red)) }.exceptionOrNull()
        val zero = runCatching { Modifier.colorPulse(durationMs = 0) }.exceptionOrNull()
        assertThat(one).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(zero).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun renders_without_crashing() {
        composeRule.setContent {
            Box(
                modifier = Modifier.size(24.dp).testTag("cp").colorPulse(),
            )
        }
        composeRule.onNodeWithTag("cp").assertExists()
    }
}
