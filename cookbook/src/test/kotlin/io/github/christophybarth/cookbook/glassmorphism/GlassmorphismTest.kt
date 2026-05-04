/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("DEPRECATION") // tests for the deprecated modifier

package io.github.christophybarth.cookbook.glassmorphism

import androidx.compose.foundation.layout.Box
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
class GlassmorphismTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_negative_blur() {
        val ex = runCatching { Modifier.glassmorphism(blurRadius = (-1).dp) }.exceptionOrNull()
        assertThat(ex).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun renders_without_crashing() {
        composeRule.setContent {
            Box(
                modifier = Modifier.size(48.dp).testTag("g").glassmorphism(),
            )
        }
        composeRule.onNodeWithTag("g").assertExists()
    }
}
