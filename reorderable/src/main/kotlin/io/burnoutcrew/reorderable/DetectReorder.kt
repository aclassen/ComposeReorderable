/*
 * Copyright 2021 André Claßen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.burnoutcrew.reorderable

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.detectReorder(
    state: ReorderableState,
    key: () -> (Any),
    orientation: Orientation = Orientation.Vertical,
) =
    this.then(
        Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    state.interactionSource.tryEmit(ReorderInteraction.Start(key()))
                },
                onDragEnd = {
                    state.interactionSource.tryEmit(ReorderInteraction.End)
                },
                onDragCancel = {
                    state.interactionSource.tryEmit(ReorderInteraction.End)
                },
                onDrag = { change, dragAmount ->
                    change.consumeAllChanges()
                    state.interactionSource.tryEmit(ReorderInteraction.Drag(dragAmount.forOrientation(orientation)))
                })
        }
    )

fun Modifier.detectReorderAfterLongPress(
    state: ReorderableState,
    key: () -> (Any),
    orientation: Orientation = Orientation.Vertical,
) =
    this.then(
        Modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    state.interactionSource.tryEmit(ReorderInteraction.Start(key()))
                },
                onDragEnd = {
                    state.interactionSource.tryEmit(ReorderInteraction.End)
                },
                onDragCancel = {
                    state.interactionSource.tryEmit(ReorderInteraction.End)
                },
                onDrag = { change, dragAmount ->
                    change.consumeAllChanges()
                    state.interactionSource.tryEmit(ReorderInteraction.Drag(dragAmount.forOrientation(orientation)))
                })
        }
    )

fun Modifier.detectListReorder(
    state: ReorderableState,
    orientation: Orientation = Orientation.Vertical,
    isDragEnabled: ((index: Int) -> Boolean)? = null,
) =
    this.then(
        Modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    val off = offset.forOrientation(orientation).toInt()
                    state.listState.layoutInfo.visibleItemsInfo
                        .firstOrNull { off in it.offset..(it.offset + it.size) }
                        ?.takeIf { isDragEnabled?.invoke(it.index) != false }
                        ?.also {
                            state.interactionSource.tryEmit(ReorderInteraction.Start(it.key))
                        }
                },
                onDragEnd = {
                    state.interactionSource.tryEmit(ReorderInteraction.End)
                },
                onDragCancel = {
                    state.interactionSource.tryEmit(ReorderInteraction.End)
                },
                onDrag = { change, dragAmount ->
                    change.consumeAllChanges()
                    state.interactionSource.tryEmit(ReorderInteraction.Drag(dragAmount.forOrientation(orientation)))
                })
        })

private fun Offset.forOrientation(orientation: Orientation) = if (orientation == Orientation.Vertical) y else x