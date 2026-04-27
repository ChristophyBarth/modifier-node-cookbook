/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.skeleton

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
class SkeletonTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shape_change_keeps_node_attached() {
        var shape: Shape by mutableStateOf(RoundedCornerShape(4.dp))
        composeRule.setContent {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(48.dp).testTag("sk").skeleton(shape = shape, color = Color.LightGray),
            )
        }
        composeRule.onNodeWithTag("sk").assertExists()
        composeRule.runOnIdle { shape = RoundedCornerShape(16.dp) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("sk").assertExists()
    }
}
