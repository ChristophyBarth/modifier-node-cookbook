/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookbookSampleApp()
        }
    }
}

@Composable
private fun CookbookSampleApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val entries = remember { catalogEntries() }
            // Saved as String because CatalogEntry holds a @Composable lambda (not Saveable);
            // we resolve back to the entry through `entries`.
            var currentId by rememberSaveable { mutableStateOf<String?>(null) }
            val menuListState = rememberLazyListState()
            val active = currentId?.let { id -> entries.firstOrNull { it.id == id } }
            if (active == null) {
                CatalogMenu(
                    entries = entries,
                    listState = menuListState,
                    onSelect = { currentId = it.id },
                )
            } else {
                BackHandler { currentId = null }
                DemoDetail(entry = active, onBack = { currentId = null })
            }
        }
    }
}
