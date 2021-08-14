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

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.ui.util.fastFirstOrNull
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sign

internal class ReorderLogic(
    private val state: ReorderableState,
    private val onMove: (fromIndex: Int, toIndex: Int) -> (Unit),
    private val canDragOver: ((index: Int) -> Boolean)? = null,
    private val onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
) {
    fun startDrag(key: Any) =
        state.listState.layoutInfo.visibleItemsInfo
            .fastFirstOrNull { it.key == key }
            ?.also { info ->
                state.selected = info
                state.draggedIndex = info.index
            }

    suspend fun dragBy(amount: Float): Boolean =
        state.draggedIndex?.let {
            state.movedDist += amount
            checkIfMoved()
            true
        } ?: false

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
            state.draggedIndex?.also { draggedItem ->
                chooseDropIndex(
                    state.listState.layoutInfo.visibleItemsInfo
                        .filterNot { it.offsetEnd() < start || it.offset > end || it.index == draggedItem }
                        .filter { canDragOver?.invoke(it.index) != false },
                    start,
                    end
                )?.also { targetIdx ->
                    onMove(draggedItem, targetIdx)
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
        draggedItem.let { draggedItem ->
            var targetIndex: Int? = null
            if (draggedItem != null) {
                val distance = curStart - draggedItem.offset
                if (distance != 0f) {
                    var targetDiff = -1f
                    for (index in items.indices) {
                        val item = items[index]
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
            } else if (state.draggedIndex != null) {
                targetIndex = items.lastOrNull()?.index
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

internal fun LazyListLayoutInfo.itemInfoByIndex(index: Int) =
    visibleItemsInfo.getOrNull(index - visibleItemsInfo.first().index)