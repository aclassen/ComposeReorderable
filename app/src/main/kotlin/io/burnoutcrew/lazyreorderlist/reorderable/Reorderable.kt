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
package io.burnoutcrew.lazyreorderlist.reorderable

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.reorderable(
    state: ReorderableState,
    orientation: Orientation = Orientation.Vertical,
    maxScrollPerFrame: Dp = 20.dp,
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val job = remember { mutableStateOf<Job?>(null) }
    val maxScroll = with(LocalDensity.current) { maxScrollPerFrame.toPx() }
    Modifier.pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                state.startDrag((if (orientation == Orientation.Vertical) offset.y else offset.x).toInt())
            },
            onDragEnd = {
                job.value?.cancel()
                state.endDrag()
            },
            onDragCancel = {
                job.value?.cancel()
                state.endDrag()
            },
            onDrag = { change, dragAmount ->
                change.consumeAllChanges()
                if (!state.dragBy(if (orientation == Orientation.Vertical) dragAmount.y else dragAmount.x)) {
                    return@detectDragGesturesAfterLongPress
                }
                if (job.value?.isActive == true) {
                    return@detectDragGesturesAfterLongPress
                }
                state.calcAutoScrollOffset(0, maxScroll)
                    .takeIf { it != 0f }
                    ?.also {
                        job.value = scope.launch {
                            var scroll = it
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
                    } ?: job.value?.cancel()
            })
    }
}