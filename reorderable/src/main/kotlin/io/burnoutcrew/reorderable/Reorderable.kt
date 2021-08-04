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

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.reorderable(
    state: ReorderableState,
    orientation: Orientation = Orientation.Vertical,
    maxScrollPerFrame: Dp = 20.dp,
): Modifier = composed {
    val job = remember { mutableStateOf<Job?>(null) }
    val maxScroll = with(LocalDensity.current) { maxScrollPerFrame.toPx() }
    val channel = remember { Channel<Float>(Channel.UNLIMITED) }
    LaunchedEffect(state) {
        channel.consumeEach { dragAmount ->
            when {
                dragAmount == Float.NEGATIVE_INFINITY -> {
                    job.value?.cancel()
                    state.endDrag()
                }
                state.draggedIndex == null -> {
                    state.startDrag(dragAmount.toInt())
                }
                state.dragBy(dragAmount) && job.value?.isActive != true -> {
                    val scrollOffset = state.calcAutoScrollOffset(0, maxScroll)
                    if (scrollOffset != 0f) {
                        job.value = launch {
                            var scroll = scrollOffset
                            var start = 0L
                            while (scroll != 0f && isActive) {
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
                        job.value?.cancel()
                    }
                }
            }
        }
    }
    Modifier.pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                channel.trySend((if (orientation == Orientation.Vertical) offset.y else offset.x))
            },
            onDragEnd = {
                channel.trySend(Float.NEGATIVE_INFINITY)
            },
            onDragCancel = {
                channel.trySend(Float.NEGATIVE_INFINITY)
            },
            onDrag = { change, dragAmount ->
                change.consumeAllChanges()
                channel.trySend(if (orientation == Orientation.Vertical) dragAmount.y else dragAmount.x)
            })
    }
}