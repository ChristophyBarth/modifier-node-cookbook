/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.parallax

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ParallaxTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun factor_change_keeps_node_attached() {
        var factor by mutableFloatStateOf(0.5f)
        composeRule.setContent {
            val scroll = remember { ScrollState(0) }
            Box(modifier = Modifier.size(48.dp).testTag("p").parallax(scroll, factor = factor))
        }
        composeRule.onNodeWithTag("p").assertExists()
        composeRule.runOnIdle { factor = -0.3f }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("p").assertExists()
    }
}
