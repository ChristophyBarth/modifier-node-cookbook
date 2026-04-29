/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.gradientborder

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
class GradientBorderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_degenerate_args() {
        val singleColor = runCatching { Modifier.gradientBorder(colors = listOf(Color.Red)) }.exceptionOrNull()
        val negWidth = runCatching { Modifier.gradientBorder(width = (-1).dp) }.exceptionOrNull()
        val zeroDur = runCatching { Modifier.gradientBorder(durationMillis = 0) }.exceptionOrNull()
        assertThat(singleColor).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(negWidth).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(zeroDur).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun static_mode_renders_without_crashing() {
        composeRule.setContent {
            Box(
                modifier = Modifier.size(48.dp).testTag("gb").gradientBorder(animate = false),
            )
        }
        composeRule.onNodeWithTag("gb").assertExists()
    }
}
