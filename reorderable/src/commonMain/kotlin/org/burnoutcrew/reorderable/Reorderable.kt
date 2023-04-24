/*
 * Copyright 2022 André Claßen
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
package org.burnoutcrew.reorderable

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow

fun Modifier.reorderable(
    state: ReorderableState<*>
) = then(
    Modifier.onGloballyPositioned { state.layoutWindowPosition.value = it.positionInWindow()}.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)

            detectDrag(
                down.id,
                onDragEnd = state::onDragCanceled,
                onDragCancel = state::onDragCanceled,
                onDrag = { event, amount ->
                    if (state.draggingItemIndex != null){
                        state.onDrag(amount.x.toInt(), amount.y.toInt())
                        event.consume()
                    }
                }
            )

        }
    }
)

internal suspend fun AwaitPointerEventScope.detectDrag(
    down: PointerId,
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    if (
        drag(down) {
            onDrag(it, it.positionChange())
        }
    ) {
        // consume up if we quit drag gracefully with the up
        currentEvent.changes.forEach {
            if (it.changedToUp()) it.consume()
        }
        onDragEnd()
    } else {
        onDragCancel()
    }
}