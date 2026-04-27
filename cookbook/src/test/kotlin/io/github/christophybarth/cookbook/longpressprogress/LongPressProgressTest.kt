/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.longpressprogress

import androidx.compose.foundation.layout.size
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
class LongPressProgressTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_zero_duration_or_negative_stroke() {
        val zeroDuration = runCatching {
            Modifier.longPressProgress(durationMs = 0L) { }
        }.exceptionOrNull()
        val negStroke = runCatching {
            Modifier.longPressProgress(strokeWidth = (-1).dp) { }
        }.exceptionOrNull()
        assertThat(zeroDuration).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(negStroke).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun renders_without_initial_progress() {
        composeRule.setContent {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(48.dp).testTag("lpp").longPressProgress { },
            )
        }
        composeRule.onNodeWithTag("lpp").assertExists()
    }
}
