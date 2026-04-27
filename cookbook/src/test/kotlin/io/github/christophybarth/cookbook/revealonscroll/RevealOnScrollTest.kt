/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.revealonscroll

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
class RevealOnScrollTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_threshold_outside_unit_range() {
        val negative = runCatching { Modifier.revealOnScroll(thresholdFraction = -0.1f) }.exceptionOrNull()
        val above = runCatching { Modifier.revealOnScroll(thresholdFraction = 1.1f) }.exceptionOrNull()
        assertThat(negative).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(above).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun renders_without_crashing() {
        composeRule.setContent {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(48.dp).testTag("r").revealOnScroll(),
            )
        }
        composeRule.onNodeWithTag("r").assertExists()
    }
}
