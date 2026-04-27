/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.tilt

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
class TiltTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_negative_max_angle() {
        val ex = runCatching { Modifier.tilt(maxAngleDeg = -1f) }.exceptionOrNull()
        assertThat(ex).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun max_angle_change_keeps_node_attached() {
        var maxAngle by mutableFloatStateOf(12f)
        composeRule.setContent {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(48.dp).testTag("t").tilt(maxAngleDeg = maxAngle),
            )
        }
        composeRule.onNodeWithTag("t").assertExists()
        composeRule.runOnIdle { maxAngle = 24f }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("t").assertExists()
    }
}
