/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.fadeedges

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
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
class FadeScrollEdgesTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_negative_fade_length() {
        val state = ScrollState(0)
        val ex = runCatching { Modifier.fadeScrollEdges(state, fadeLength = (-1).dp) }.exceptionOrNull()
        assertThat(ex).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun renders_with_scroll_state_without_crashing() {
        composeRule.setContent {
            val scroll = remember { ScrollState(0) }
            Box(
                modifier = Modifier.size(80.dp).testTag("container").fadeScrollEdges(scroll),
            ) {
                Box(modifier = Modifier.verticalScroll(scroll).size(width = 80.dp, height = 400.dp))
            }
        }
        composeRule.onNodeWithTag("container").assertExists()
    }
}
