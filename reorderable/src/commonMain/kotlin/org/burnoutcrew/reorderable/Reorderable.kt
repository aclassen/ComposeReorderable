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
package org.burnoutcrew.reorderable

import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
fun Modifier.reorderable(
    state: ReorderableState<*>,
    maxScrollPerFrame: Dp = 20.dp,
) = composed {
    val job: MutableState<Job?> = remember { mutableStateOf(null) }
    val maxScroll = with(LocalDensity.current) { maxScrollPerFrame.toPx() }
    val scope = rememberCoroutineScope()
    val interactions = remember { MutableSharedFlow<ReorderAction>(extraBufferCapacity = 16) }
    fun cancelAutoScroll() {
        job.value = job.value?.let {
            it.cancel()
            null
        }
    }
    LaunchedEffect(state) {
        merge(
            interactions,
            snapshotFlow { state.visibleItemsInfo }
                .map { ReorderAction.Drag(0f, 0f) }
        )
            .collect { event ->
                when (event) {
                    is ReorderAction.End -> {
                        cancelAutoScroll()
                        state.endDrag()
                    }
                    is ReorderAction.Start -> {
                        state.startDrag(event.key)
                    }
                    is ReorderAction.Drag -> {
                        if (state.dragBy(event.amount.toInt(), event.amountY?.toInt() ?: 0) && job.value?.isActive != true) {
                            val scrollOffset = state.calcAutoScrollOffset(0, maxScroll)
                            if (scrollOffset != 0f) {
                                job.value =
                                    scope.launch {
                                        var scroll = scrollOffset
                                        var start = 0L
                                        while (scroll != 0f && job.value?.isActive == true) {
                                            withFrameMillis {
                                                if (start == 0L) {
                                                    start = it
                                                } else {
                                                    scroll = state.calcAutoScrollOffset(it - start, maxScroll)
                                                }
                                            }
                                            if (state.scrollBy(scroll) != scroll) {
                                                scroll = 0f
                                            }
                                        }
                                    }
                            } else {
                                cancelAutoScroll()
                            }
                        }
                    }
                }
            }
    }

    Modifier.pointerInput(Unit) {
        forEachGesture {
            val dragStart = state.ch.receive()
            val down = awaitPointerEventScope {
                currentEvent.changes.fastFirstOrNull { it.id == dragStart.id }
            }

            val item = down?.position?.let { position ->
                state.findKeyAt(position.x, position.y)
            }
            if (down != null && item != null) {

                interactions.tryEmit(ReorderAction.Start(item))
                dragStart.offet?.also {
                    interactions.tryEmit(ReorderAction.Drag(it.x, it.y))
                }
                detectDrag(
                    down.id,
                    onDragEnd = { interactions.tryEmit(ReorderAction.End) },
                    onDragCancel = { interactions.tryEmit(ReorderAction.End) },
                    onDrag = { change, dragAmount ->
                        change.consumeAllChanges()
                        interactions.tryEmit(ReorderAction.Drag(dragAmount.x, dragAmount.y))
                    })
            }
        }
    }
}

internal suspend fun PointerInputScope.detectDrag(
    down: PointerId,
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    awaitPointerEventScope {
        if (
            drag(down) {
                onDrag(it, it.positionChange())
                it.consumePositionChange()
            }
        ) {
            // consume up if we quit drag gracefully with the up
            currentEvent.changes.forEach {
                if (it.changedToUp()) {
                    it.consumeDownChange()
                }
            }
            onDragEnd()
        } else {
            onDragCancel()
        }
    }
}

internal sealed class ReorderAction {
    class Start(val key: Any) : ReorderAction()
    class Drag(val amount: Float, val amountY: Float? = null) : ReorderAction()
    object End : ReorderAction()
}

internal data class StartDrag(val id: PointerId, val offet: Offset? = null)