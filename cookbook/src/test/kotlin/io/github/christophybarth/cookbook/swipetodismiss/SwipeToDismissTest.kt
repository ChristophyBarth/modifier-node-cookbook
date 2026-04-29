/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.swipetodismiss

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
class SwipeToDismissTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_threshold_outside_unit_range() {
        val ex = runCatching { Modifier.swipeToDismiss(onDismiss = {}, threshold = 1.5f) }.exceptionOrNull()
        assertThat(ex).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun renders_without_crashing() {
        composeRule.setContent {
            Box(
                modifier = Modifier.size(48.dp).testTag("s").swipeToDismiss(onDismiss = {}),
            )
        }
        composeRule.onNodeWithTag("s").assertExists()
    }
}
