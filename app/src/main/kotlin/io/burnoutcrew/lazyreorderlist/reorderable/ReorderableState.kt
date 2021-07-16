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

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sign

@Composable
fun rememberReorderState(
    listState: LazyListState = rememberLazyListState(),
    onMove: (fromPos: Int, toPos: Int) -> (Unit),
    canDragOver: ((index: Int) -> Boolean)? = null,
    isDragEnabled: ((index: Int) -> Boolean)? = null,
    onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
): ReorderableState =
    remember {
        ReorderableState(listState, onMove, canDragOver, isDragEnabled, onDragEnd)
    }

class ReorderableState(
    val listState: LazyListState,
    private val onMove: (fromIndex: Int, toIndex: Int) -> (Unit),
    private val canDragOver: ((index: Int) -> Boolean)? = null,
    private val isDragEnabled: ((index: Int) -> Boolean)? = null,
    private val onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
) {
    private fun LazyListItemInfo.offsetEnd() =
        offset + size

    private fun LazyListLayoutInfo.itemInfoByIndex(index: Int) =
        visibleItemsInfo.getOrNull(index - visibleItemsInfo.first().index)

    private var selected by mutableStateOf<LazyListItemInfo?>(null)
    private var movedDist by mutableStateOf(0f)
    private val draggedItem get() = index?.let { listState.layoutInfo.itemInfoByIndex(it) }
    private val viewportStartOffset get() = listState.layoutInfo.viewportStartOffset.toFloat()
    private val viewportEndOffset get() = listState.layoutInfo.viewportEndOffset.toFloat()

    var index by mutableStateOf<Int?>(null)
        internal set

    val offset by derivedStateOf {
        index
            ?.let { listState.layoutInfo.itemInfoByIndex(it) }
            ?.let { (selected?.offset?.toFloat() ?: 0f) + movedDist - it.offset }
    }

    fun startDrag(offset: Int) =
        listState.layoutInfo.visibleItemsInfo
            .firstOrNull { offset in it.offset..it.offsetEnd() }
            ?.takeIf { isDragEnabled?.invoke(it.index) != false }
            ?.also { info ->
                selected = info
                index = info.index
            }

    fun dragBy(amount: Float): Boolean {
        if (index != null) {
            movedDist += amount
            checkIfMoved()
            return true
        }
        return false
    }

    fun endDrag() {
        val startIndex = selected?.index
        val endIndex = index
        index = null
        selected = null
        movedDist = 0f
        onDragEnd?.apply {
            if (startIndex != null && endIndex != null) {
                invoke(startIndex, endIndex)
            }
        }
    }

    suspend fun scrollBy(value: Float) =
        listState.scrollBy(value)
            .also {
                if (it != 0f) checkIfMoved()
            }

    fun calcAutoScrollOffset(time: Long, maxScroll: Float): Float =
        selected?.let { selected ->
            val start = (movedDist + selected.offset)
            when {
                movedDist < 0 -> (start - viewportStartOffset).takeIf { it < 0 }
                movedDist > 0 -> (start + selected.size - viewportEndOffset).takeIf { it > 0 }
                else -> null
            }
                ?.takeIf { it != 0f }
                ?.let { interpolateOutOfBoundsScroll(selected.size, it, time, maxScroll) }
        } ?: 0f

    private fun checkIfMoved() {
        selected?.also { selected ->
            val start = (movedDist + selected.offset)
                .coerceIn(viewportStartOffset - selected.size, viewportEndOffset)
            val end = (start + selected.size)
                .coerceIn(viewportStartOffset, viewportEndOffset + selected.size)
            draggedItem?.also { draggedItem ->
                chooseDropIndex(
                    listState.layoutInfo.visibleItemsInfo
                        .filterNot { it.offsetEnd() < start || it.offset > end || it.index == draggedItem.index }
                        .filter { canDragOver?.invoke(it.index) != false }, start, end
                )?.also { targetIdx ->
                    onMove(draggedItem.index, targetIdx)
                    index = targetIdx
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