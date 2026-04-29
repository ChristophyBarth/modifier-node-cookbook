/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.composites

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.shimmer.shimmer
import io.github.christophybarth.cookbook.skeleton.skeleton

/**
 * Combines [skeleton] and [shimmer] into the canonical "content is loading" placeholder.
 *
 * Wired as `shimmer().skeleton(...)`: shimmer must be outer because [skeleton] does not call
 * `drawContent()` and would otherwise short-circuit any inner draw modifier.
 *
 * @param shape Outline used by the skeleton fill.
 * @param color Fill colour for the skeleton.
 *
 * @sample io.github.christophybarth.cookbook.samples.LoadingPlaceholderSample
 */
public fun Modifier.loadingPlaceholder(
    shape: Shape = RoundedCornerShape(8.dp),
    color: Color = Color(0xFFE0E0E0),
): Modifier = this.shimmer().skeleton(shape = shape, color = color)
