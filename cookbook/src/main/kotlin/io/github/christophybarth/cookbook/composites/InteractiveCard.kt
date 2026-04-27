/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.composites

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.christophybarth.cookbook.hoverelevation.hoverElevation
import io.github.christophybarth.cookbook.pressscale.pressScale
import io.github.christophybarth.cookbook.tilt.tilt

/**
 * Combines [pressScale], [tilt], and [hoverElevation] into a single ergonomic modifier with
 * sensible defaults. Drop on any tappable card to get the full premium feel (press shrink,
 * pointer tilt, hover lift) with one line.
 *
 * The defaults are tuned to be subtler than each modifier's standalone defaults: when you
 * combine three motion sources, less of each reads as cohesive rather than busy.
 *
 * Order of application (chain order in the modifier list):
 * 1. `hoverElevation` (lifts the layer)
 * 2. `tilt` (3D rotation on the lifted layer)
 * 3. `pressScale` (final scale-on-press)
 *
 * @param pressedScale Scale during press. Defaults to a gentle `0.97f`.
 * @param tiltAngleDeg Maximum tilt angle on each axis. Defaults to `8f`, half of standalone tilt.
 * @param restingElevation Shadow when not hovered.
 * @param hoveredElevation Shadow when pointer is over the card.
 * @param shape Outline used for the elevated layer.
 *
 * @sample io.github.christophybarth.cookbook.samples.InteractiveCardSample
 */
public fun Modifier.interactiveCard(
    pressedScale: Float = 0.97f,
    tiltAngleDeg: Float = 8f,
    restingElevation: Dp = 2.dp,
    hoveredElevation: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(16.dp),
): Modifier = this
    .hoverElevation(restingDp = restingElevation, hoveredDp = hoveredElevation, shape = shape)
    .tilt(maxAngleDeg = tiltAngleDeg)
    .pressScale(scale = pressedScale)
