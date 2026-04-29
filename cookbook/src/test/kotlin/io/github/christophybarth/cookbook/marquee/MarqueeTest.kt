/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.marquee

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
class MarqueeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rejects_degenerate_args() {
        val zeroVel = runCatching { Modifier.marquee(velocityDpPerSec = 0.dp) }.exceptionOrNull()
        val negGap = runCatching { Modifier.marquee(gap = (-1).dp) }.exceptionOrNull()
        val negFade = runCatching { Modifier.marquee(fadeEdges = (-1).dp) }.exceptionOrNull()
        assertThat(zeroVel).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(negGap).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(negFade).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun renders_short_content_without_crashing() {
        composeRule.setContent {
            // Inner content is narrower than the container, so marquee is a no-op.
            Box(
                modifier = Modifier.width(160.dp).testTag("mq").marquee(),
            ) {
                Box(modifier = Modifier.size(40.dp))
            }
        }
        composeRule.onNodeWithTag("mq").assertExists()
    }
}
