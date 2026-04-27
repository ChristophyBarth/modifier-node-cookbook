/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.dragtoreorder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.remember
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
class DragToReorderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun state_holds_initial_order() {
        val state = ReorderableState(listOf("a", "b", "c"))
        assertThat(state.items).containsExactly("a", "b", "c").inOrder()
    }

    @Test
    fun renders_rows_without_crashing() {
        composeRule.setContent {
            val state = remember { ReorderableState(listOf("a", "b", "c")) }
            Column {
                state.items.forEach { row ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("row-$row")
                            .dragToReorder(state, row),
                    )
                }
            }
        }
        composeRule.onNodeWithTag("row-a").assertExists()
        composeRule.onNodeWithTag("row-b").assertExists()
        composeRule.onNodeWithTag("row-c").assertExists()
    }
}
