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
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

fun <T> Modifier.reorderable(
    state: ReorderableState,
    items: SnapshotStateList<T>,
    orientation: Orientation = Orientation.Vertical,
    autoScrollAnimationSpec: AnimationSpec<Float> = tween(1000, easing = LinearEasing),
): Modifier = reorderable(
    state,
    { fromPos, toPos -> items.move(fromPos, toPos) },
    orientation,
    autoScrollAnimationSpec
)

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.reorderable(
    state: ReorderableState,
    onMove: (Int, Int) -> (Unit),
    orientation: Orientation = Orientation.Vertical,
    autoScrollAnimationSpec: AnimationSpec<Float> = tween(1000, easing = LinearEasing),
): Modifier = composed {
    var autoScrollViewportOffset by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(state) {
        snapshotFlow { state.listState.layoutInfo }
            .combine(snapshotFlow { state.position }.distinctUntilChanged()) { layoutInfo, pos ->
                pos?.let { draggedCenter ->
                    layoutInfo.visibleItemsInfo
                        .minByOrNull { (draggedCenter - (it.offset + it.size / 2f)).absoluteValue }
                }?.index
            }
            .distinctUntilChanged()
            .collect { nearest ->
                state.index = state.index.let { index ->
                    when {
                        nearest == null -> null
                        index == null -> nearest
                        else -> nearest.also { onMove(index, it) }
                    }
                }
            }
    }
    Modifier.pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                val startOffset = (if (orientation == Orientation.Vertical) offset.y else offset.x).toInt()
                state.listState.layoutInfo.visibleItemsInfo
                    .firstOrNull { startOffset in it.offset..it.offset + it.size }
                    ?.also {
                        autoScrollViewportOffset = it.size / 2
                        state.position = it.offset + it.size / 2f
                    }
            },
            onDragEnd = {
                scope.launch { state.listState.autoScrollBy(0) }
                state.position = null
            },
            onDragCancel = {
                scope.launch { state.listState.autoScrollBy(0) }
                state.position = null
            },
            onDrag = { change, dragAmount ->
                change.consumeAllChanges()
                val amount = if (orientation == Orientation.Vertical) dragAmount.y else dragAmount.x
                state.position = state.position?.plus(amount)
                (state.position?.let { pos ->
                    when {
                        (state.listState.isScrollInProgress || amount > 0)
                                && !state.listState.isLastItemVisible()
                                && pos > state.listState.layoutInfo.viewportEndOffset - autoScrollViewportOffset -> {
                            state.listState.viewPortSize()
                        }
                        (state.listState.isScrollInProgress || amount < 0)
                                && state.listState.firstVisibleItemIndex > 0
                                && pos <= state.listState.layoutInfo.viewportStartOffset + autoScrollViewportOffset -> {
                            -state.listState.viewPortSize()
                        }
                        else -> 0
                    }
                } ?: 0)
                    .also {
                        if (!state.listState.isScrollInProgress || it == 0) {
                            scope.launch { state.listState.autoScrollBy(it, autoScrollAnimationSpec) }
                        }
                    }
            })
    }
}

private fun LazyListState.isLastItemVisible() =
    firstVisibleItemIndex + layoutInfo.visibleItemsInfo.size >= layoutInfo.totalItemsCount

private fun LazyListState.viewPortSize() =
    layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset

private suspend fun LazyListState.autoScrollBy(
    value: Int,
    animationSpec: AnimationSpec<Float> = tween(1000, easing = LinearEasing)
) {
    if (value == 0) {
        stopScroll(MutatePriority.PreventUserInput)
    } else {
        var loop = true
        while (loop) {
            loop = animateScrollBy(value.toFloat(), animationSpec) == value.toFloat()
        }
    }
}

fun <T> MutableList<T>.move(fromIdx: Int, toIdx: Int) {
    when {
        fromIdx == toIdx -> {
            return
        }
        toIdx > fromIdx -> {
            for (i in fromIdx until toIdx) {
                this[i] = this[i + 1].also { this[i + 1] = this[i] }
            }
        }
        else -> {
            for (i in fromIdx downTo toIdx + 1) {
                this[i] = this[i - 1].also { this[i - 1] = this[i] }
            }
        }
    }
}