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

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sign

@Composable
fun rememberReorderState(
    listState: LazyListState = rememberLazyListState(),
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
) =
    remember { ReorderableState(listState, interactionSource) }

class ReorderableState(
    val listState: LazyListState,
    val interactionSource: MutableInteractionSource,
) {
    var draggedIndex by mutableStateOf<Int?>(null)
        internal set

    @Suppress("MemberVisibilityCanBePrivate")
    val draggedKey by derivedStateOf { selected?.key }

    @Suppress("MemberVisibilityCanBePrivate")
    val draggedOffset by derivedStateOf {
        draggedIndex
            ?.let { listState.layoutInfo.itemInfoByIndex(it) }
            ?.let { (selected?.offset?.toFloat() ?: 0f) + movedDist - it.offset }
    }

    fun offsetOf(key: Any) =
        if (draggedKey == key) draggedOffset else null

    fun offsetOf(index: Int) =
        if (draggedIndex == index) draggedOffset else null

    internal var selected by mutableStateOf<LazyListItemInfo?>(null)
    internal var movedDist by mutableStateOf(0f)
}

@Composable
fun Reorderable(
    state: ReorderableState,
    onMove: (fromPos: Int, toPos: Int) -> (Unit),
    canDragOver: ((index: Int) -> Boolean)? = null,
    onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
    maxScrollPerFrame: Dp = 20.dp,
) {
    var job: Job? by remember { mutableStateOf(null) }
    val maxScroll = with(LocalDensity.current) { maxScrollPerFrame.toPx() }
    val logic = remember { ReorderLogic(state, onMove, canDragOver, onDragEnd) }
    val scope = rememberCoroutineScope()
    fun cancelAutoScroll() {
        job = job?.let {
            it.cancel()
            null
        }
    }
    LaunchedEffect(state) {
        state.interactionSource.interactions.collect { event ->
            when (event) {
                is ReorderInteraction.End -> {
                    cancelAutoScroll()
                    logic.endDrag()
                }
                is ReorderInteraction.Start -> {
                    logic.startDrag(event.key)
                }
                is ReorderInteraction.Drag -> {
                    if (logic.dragBy(event.amount) && job?.isActive != true) {
                        val scrollOffset = logic.calcAutoScrollOffset(0, maxScroll)
                        if (scrollOffset != 0f) {
                            job =
                                scope.launch {
                                    var scroll = scrollOffset
                                    var start = 0L
                                    while (scroll != 0f && job?.isActive == true) {
                                        withFrameMillis {
                                            if (start == 0L) {
                                                start = it
                                            } else {
                                                scroll = logic.calcAutoScrollOffset(it - start, maxScroll)
                                            }
                                        }
                                        if (logic.scrollBy(scroll) != scroll) {
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
}

class ReorderLogic(
    private val state: ReorderableState,
    private val onMove: (fromIndex: Int, toIndex: Int) -> (Unit),
    private val canDragOver: ((index: Int) -> Boolean)? = null,
    private val onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
) {
    fun startDrag(key: Any) =
        state.listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.key == key }
            ?.also { info ->
                state.selected = info
                state.draggedIndex = info.index
            }

    suspend fun dragBy(amount: Float): Boolean {
        if (state.draggedIndex != null) {
            state.movedDist += amount
            checkIfMoved()
            return true
        }
        return false
    }

    fun endDrag() {
        val startIndex = state.selected?.index
        val endIndex = state.draggedIndex
        state.draggedIndex = null
        state.selected = null
        state.movedDist = 0f
        onDragEnd?.apply {
            if (startIndex != null && endIndex != null) {
                invoke(startIndex, endIndex)
            }
        }
    }

    suspend fun scrollBy(value: Float) =
        state.listState.scrollBy(value)
            .also {
                if (it != 0f) checkIfMoved()
            }

    fun calcAutoScrollOffset(time: Long, maxScroll: Float): Float =
        state.selected?.let { selected ->
            val start = (state.movedDist + selected.offset)
            when {
                state.movedDist < 0 -> (start - viewportStartOffset).takeIf { it < 0 }
                state.movedDist > 0 -> (start + selected.size - viewportEndOffset).takeIf { it > 0 }
                else -> null
            }
                ?.takeIf { it != 0f }
                ?.let { interpolateOutOfBoundsScroll(selected.size, it, time, maxScroll) }
        } ?: 0f

    private suspend fun checkIfMoved() {
        state.selected?.also { selected ->
            val start = (state.movedDist + selected.offset)
                .coerceIn(viewportStartOffset - selected.size, viewportEndOffset)
            val end = (start + selected.size)
                .coerceIn(viewportStartOffset, viewportEndOffset + selected.size)
            draggedItem?.also { draggedItem ->
                chooseDropIndex(
                    state.listState.layoutInfo.visibleItemsInfo
                        .filterNot { it.offsetEnd() < start || it.offset > end || it.index == draggedItem.index }
                        .filter { canDragOver?.invoke(it.index) != false }, start, end
                )?.also { targetIdx ->
                    onMove(draggedItem.index, targetIdx)
                    state.draggedIndex = targetIdx
                    state.listState.scrollToItem(state.listState.firstVisibleItemIndex, state.listState.firstVisibleItemScrollOffset)
                }
            }
        }
    }

    private fun chooseDropIndex(
        items: List<LazyListItemInfo>,
        curStart: Float,
        curEnd: Float,
    ): Int? =
        draggedItem?.let { draggedItem ->
            var targetIndex: Int? = null
            val distance = curStart - draggedItem.offset
            if (distance != 0f) {
                var targetDiff = -1f
                items.forEach { item ->
                    (when {
                        distance > 0 -> (item.offsetEnd() - curEnd)
                            .takeIf { diff -> diff < 0 && item.offsetEnd() > draggedItem.offsetEnd() }
                        else -> (item.offset - curStart)
                            .takeIf { diff -> diff > 0 && item.offset < draggedItem.offset }
                    })
                        ?.absoluteValue
                        ?.takeIf { it > targetDiff }
                        ?.also {
                            targetDiff = it
                            targetIndex = item.index
                        }
                }
            }
            targetIndex
        }

    private fun LazyListItemInfo.offsetEnd() =
        offset + size

    private val draggedItem get() = state.draggedIndex?.let { state.listState.layoutInfo.itemInfoByIndex(it) }
    private val viewportStartOffset get() = state.listState.layoutInfo.viewportStartOffset.toFloat()
    private val viewportEndOffset get() = state.listState.layoutInfo.viewportEndOffset.toFloat()

    companion object {
        private const val ACCELERATION_LIMIT_TIME_MS: Long = 1500
        private val EaseOutQuadInterpolator: (Float) -> (Float) = {
            val t = 1 - it
            1 - t * t * t * t
        }
        private val EaseInQuintInterpolator: (Float) -> (Float) = {
            it * it * it * it * it
        }

        fun interpolateOutOfBoundsScroll(
            viewSize: Int,
            viewSizeOutOfBounds: Float,
            time: Long,
            maxScroll: Float,
        ): Float {
            val outOfBoundsRatio = min(1f, 1f * viewSizeOutOfBounds.absoluteValue / viewSize)
            val cappedScroll =
                sign(viewSizeOutOfBounds) * maxScroll * EaseOutQuadInterpolator(outOfBoundsRatio)
            val timeRatio =
                if (time > ACCELERATION_LIMIT_TIME_MS) 1f else time.toFloat() / ACCELERATION_LIMIT_TIME_MS
            return (cappedScroll * EaseInQuintInterpolator(timeRatio)).let {
                if (it == 0f) {
                    if (viewSizeOutOfBounds > 0) 1f else -1f
                } else {
                    it
                }
            }
        }
    }
}

interface ReorderInteraction : Interaction {
    class Start(val key: Any) : ReorderInteraction
    class Drag(val amount: Float) : ReorderInteraction
    object End : ReorderInteraction
}

private fun LazyListLayoutInfo.itemInfoByIndex(index: Int) =
    visibleItemsInfo.getOrNull(index - visibleItemsInfo.first().index)