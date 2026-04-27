/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.magnetic

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
class MagneticTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_empty_anchors_or_negative_threshold() {
        val empty = runCatching { Modifier.magnetic(anchors = emptyList()) }.exceptionOrNull()
        val neg = runCatching {
            Modifier.magnetic(anchors = listOf(48.dp), snapThreshold = (-1).dp)
        }.exceptionOrNull()
        assertThat(empty).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(neg).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun renders_without_crashing() {
        composeRule.setContent {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(48.dp).testTag("m").magnetic(anchors = listOf(0.dp, 48.dp)),
            )
        }
        composeRule.onNodeWithTag("m").assertExists()
    }
}
