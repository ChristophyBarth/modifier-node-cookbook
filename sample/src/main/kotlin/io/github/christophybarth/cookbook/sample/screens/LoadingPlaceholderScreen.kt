/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.composites.loadingPlaceholder

@Composable
internal fun LoadingPlaceholderScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(width = 240.dp, height = 80.dp).loadingPlaceholder(RoundedCornerShape(12.dp)))
        Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).loadingPlaceholder(RoundedCornerShape(8.dp)))
        Box(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp).loadingPlaceholder(RoundedCornerShape(8.dp)))
        Box(modifier = Modifier.size(48.dp).loadingPlaceholder(CircleShape))
    }
}
