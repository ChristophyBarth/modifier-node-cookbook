/*
 * Copyright 2026 Christophy Barth
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.christophybarth.cookbook.dragtoreorder

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints

/**
 * Mutable state holder for [dragToReorder]. Hold the current item order; the modifier mutates
 * [items] in place as the user drags an item past a neighbour.
 *
 * Create with [rememberReorderableState] inside a composable. Pass `state.items` to the loop
 * that renders rows; pass `state` and the row's key to each row's `dragToReorder` modifier.
 */
@Stable
public class ReorderableState<T : Any> internal constructor(initial: List<T>) {

    public var items: List<T> by mutableStateOf(initial)
        internal set

    internal var draggingKey: T? by mutableStateOf(null)
        private set

    internal var dragOffset: Float by mutableFloatStateOf(0f)
        private set

    private val itemHeights: MutableMap<T, Int> = mutableMapOf()

    internal fun recordHeight(key: T, height: Int) {
        itemHeights[key] = height
    }

    internal fun heightOf(key: T): Int = itemHeights[key] ?: 0

    internal fun onDragStart(key: T) {
        draggingKey = key
        dragOffset = 0f
    }

    internal fun onDrag(deltaY: Float) {
        val key = draggingKey ?: return
        dragOffset += deltaY
        val itemHeight = heightOf(key).coerceAtLeast(1)
        val currentIndex = items.indexOf(key).takeIf { it >= 0 } ?: return
        val direction = when {
            dragOffset > itemHeight / 2f -> 1
            dragOffset < -itemHeight / 2f -> -1
            else -> 0
        }
        val targetIndex = currentIndex + direction
        if (direction != 0 && targetIndex in items.indices) {
            val newOrder = items.toMutableList()
            newOrder.removeAt(currentIndex)
            newOrder.add(targetIndex, key)
            items = newOrder
            // After swapping a slot, rebase the offset by one slot in the opposite direction so
            // the visual position of the dragged finger is continuous.
            dragOffset -= direction * itemHeight
        }
    }

    internal fun onDragEnd() {
        draggingKey = null
        dragOffset = 0f
    }
}

/** Convenience [ReorderableState] factory scoped to the calling composition. */
@Composable
public fun <T : Any> rememberReorderableState(initial: List<T>): ReorderableState<T> =
    remember { ReorderableState(initial) }

/**
 * Vertical drag-to-reorder for a row identified by [key]. Long-press starts the drag; a
 * subsequent drag past a neighbour's mid-point swaps the row's position in `state.items`.
 *
 * Pair with `Column { state.items.forEach { row -> Row(modifier = Modifier.dragToReorder(state, row.id)) } }`.
 *
 * Real-world reorder UX usually wants ghost/lift visuals on the dragged item; apply [shimmer],
 * [hoverElevation], or your own indication on top of `dragToReorder` to convey "lifted" state.
 *
 * @param state The shared state holder.
 * @param key Stable key identifying this row in `state.items`.
 *
 * @sample io.github.christophybarth.cookbook.samples.DragToReorderSample
 */
public fun <T : Any> Modifier.dragToReorder(
    state: ReorderableState<T>,
    key: T,
): Modifier = this then DragToReorderElement(state, key)

@Stable
private data class DragToReorderElement<T : Any>(
    val state: ReorderableState<T>,
    val key: T,
) : ModifierNodeElement<DragToReorderNode<T>>() {
    override fun create(): DragToReorderNode<T> = DragToReorderNode(state, key)
    override fun update(node: DragToReorderNode<T>) {
        node.update(state, key)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "dragToReorder"
        properties["key"] = key
    }
}

internal class DragToReorderNode<T : Any>(
    private var state: ReorderableState<T>,
    private var key: T,
) : DelegatingNode(), LayoutModifierNode {

    init {
        delegate(
            SuspendingPointerInputModifierNode {
                detectDragGesturesAfterLongPress(
                    onDragStart = { state.onDragStart(key) },
                    onDragEnd = {
                        state.onDragEnd()
                        invalidateMeasurement()
                    },
                    onDragCancel = {
                        state.onDragEnd()
                        invalidateMeasurement()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        state.onDrag(dragAmount.y)
                        invalidateMeasurement()
                    },
                )
            },
        )
    }

    fun update(newState: ReorderableState<T>, newKey: T) {
        state = newState
        key = newKey
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        state.recordHeight(key, placeable.height)
        val isMe = state.draggingKey == key
        val translateY = if (isMe) state.dragOffset.toInt() else 0
        return layout(placeable.width, placeable.height) {
            placeable.placeRelativeWithLayer(x = 0, y = translateY) {
                if (isMe) {
                    // Visual lift: small scale-up + shadow.
                    scaleX = 1.02f
                    scaleY = 1.02f
                    shadowElevation = 12f
                }
            }
        }
    }
}
