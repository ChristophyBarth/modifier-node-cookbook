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
 * Combines [skeleton] and [shimmer] into the canonical "content is loading" placeholder. The
 * skeleton paints an opaque shape; the shimmer sweeps a highlight across it.
 *
 * @param shape Outline used by the skeleton fill.
 * @param color Fill colour for the skeleton.
 *
 * @sample io.github.christophybarth.cookbook.samples.LoadingPlaceholderSample
 */
public fun Modifier.loadingPlaceholder(
    shape: Shape = RoundedCornerShape(8.dp),
    color: Color = Color(0xFFE0E0E0),
): Modifier = this.skeleton(shape = shape, color = color).shimmer()
