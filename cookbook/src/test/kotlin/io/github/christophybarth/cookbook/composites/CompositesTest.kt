/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.composites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
class CompositesTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun interactive_card_renders_without_crashing() {
        composeRule.setContent {
            Box(
                modifier = Modifier.size(96.dp).testTag("ic").interactiveCard(),
            )
        }
        composeRule.onNodeWithTag("ic").assertExists()
    }

    @Test
    fun loading_placeholder_renders_without_crashing() {
        composeRule.setContent {
            Box(
                modifier = Modifier.size(120.dp, 24.dp).testTag("lp").loadingPlaceholder(),
            )
        }
        composeRule.onNodeWithTag("lp").assertExists()
    }
}
