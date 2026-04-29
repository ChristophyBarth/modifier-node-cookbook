/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.shake

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
class ShakeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_degenerate_args() {
        val negIntensity = runCatching { Modifier.shake(trigger = 0, intensity = (-1).dp) }.exceptionOrNull()
        val zeroDur = runCatching { Modifier.shake(trigger = 0, durationMs = 0) }.exceptionOrNull()
        val zeroOsc = runCatching { Modifier.shake(trigger = 0, oscillations = 0) }.exceptionOrNull()
        assertThat(negIntensity).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(zeroDur).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(zeroOsc).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun trigger_change_keeps_node_attached() {
        var trig by mutableIntStateOf(0)
        composeRule.setContent {
            Box(
                modifier = Modifier.size(48.dp).testTag("sk").shake(trigger = trig),
            )
        }
        composeRule.onNodeWithTag("sk").assertExists()
        composeRule.runOnIdle { trig++ }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("sk").assertExists()
    }
}
