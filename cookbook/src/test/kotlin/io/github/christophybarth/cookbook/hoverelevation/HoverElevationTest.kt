/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.hoverelevation

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
import androidx.compose.ui.unit.Dp
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
class HoverElevationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_negative_elevations() {
        val negResting = runCatching { Modifier.hoverElevation(restingDp = (-1).dp) }.exceptionOrNull()
        val negHovered = runCatching { Modifier.hoverElevation(hoveredDp = (-2).dp) }.exceptionOrNull()
        assertThat(negResting).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(negHovered).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun parameter_change_keeps_node_attached() {
        var resting: Dp by mutableStateOf(2.dp)
        composeRule.setContent {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(48.dp).testTag("card").hoverElevation(restingDp = resting).background(Color.White),
            )
        }
        composeRule.onNodeWithTag("card").assertExists()
        composeRule.runOnIdle { resting = 6.dp }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("card").assertExists()
    }
}
