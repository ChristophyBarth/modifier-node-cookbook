/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.pinchzoom

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
class PinchZoomTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_invalid_scale_bounds() {
        val negMin = runCatching { Modifier.pinchZoom(minScale = 0f) }.exceptionOrNull()
        val swapped = runCatching { Modifier.pinchZoom(minScale = 4f, maxScale = 1f) }.exceptionOrNull()
        assertThat(negMin).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(swapped).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun renders_without_crashing() {
        composeRule.setContent {
            Box(
                modifier = Modifier.size(96.dp).testTag("pz").pinchZoom(),
            )
        }
        composeRule.onNodeWithTag("pz").assertExists()
    }
}
