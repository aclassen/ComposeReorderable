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

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.channels.Channel
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sign

abstract class ReorderableState<T>(
    private val onMove: (fromIndex: ItemPosition, toIndex: ItemPosition) -> (Unit),
    private val canDragOver: ((index: ItemPosition) -> Boolean)? = null,
    private val onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
) {
    protected abstract val T.left: Int
    protected abstract val T.top: Int
    protected abstract val T.right: Int
    protected abstract val T.bottom: Int
    protected abstract val T.width: Int
    protected abstract val T.height: Int
    protected abstract val T.itemIndex: Int
    protected abstract val T.itemKey: Any
    protected abstract val isVertical: Boolean
    protected abstract val viewportStartOffset: Int
    protected abstract val viewportEndOffset: Int
    protected abstract val draggedOffset: IntOffset?
    protected abstract suspend fun scrollToCurrentItem()
    abstract val visibleItemsInfo: List<T>
    abstract suspend fun scrollBy(value: Float): Float
    private val targets = mutableListOf<T>()
    private val distances = mutableListOf<Int>()
    internal val ch = Channel<StartDrag>()
    internal var selected by mutableStateOf<T?>(null)
    internal var movedDist by mutableStateOf(IntOffset.Zero)

    var draggedIndex by mutableStateOf<Int?>(null)
        internal set

    val draggedKey by derivedStateOf { selected?.itemKey }

    fun offsetByKey(key: Any) =
        if (draggedKey == key) draggedOffset else null

    fun offsetByIndex(index: Int) =
        if (draggedIndex == index) draggedOffset else null

    fun startDrag(key: Any) =
        visibleItemsInfo
            .fastFirstOrNull { it.itemKey == key }
            ?.also { info ->
                selected = info
                draggedIndex = info.itemIndex
            }

    open suspend fun dragBy(x: Int, y: Int): Boolean =
        draggedIndex?.let {
            movedDist += IntOffset(x, y)
            checkIfMoved()
            true
        } ?: false

    fun endDrag() {
        val startIndex = selected?.itemIndex
        val endIndex = draggedIndex
        draggedIndex = null
        selected = null
        movedDist = IntOffset.Zero
        onDragEnd?.apply {
            if (startIndex != null && endIndex != null) {
                invoke(startIndex, endIndex)
            }
        }
    }

    private suspend fun checkIfMoved() {
        val selected = selected ?: return
        val draggingIndex = draggedIndex ?: return
        val x = movedDist.x + selected.left
        val y = movedDist.y + selected.top
        val draggedItem = visibleItemsInfo.getOrNull(draggingIndex - visibleItemsInfo.first().itemIndex)
        chooseDropItem(draggedItem, findTargets(movedDist.x, movedDist.y, selected), x, y)
            ?.also { targetIdx ->
                onMove(ItemPosition(draggingIndex, selected.itemKey), ItemPosition(targetIdx.itemIndex, targetIdx.itemKey))
                draggedIndex = targetIdx.itemIndex
                scrollToCurrentItem()
            }
    }

    open fun findKeyAt(x: Float, y: Float): Any? {
        val posY: Int
        val posX: Int
        if (isVertical) {
            posY = (y + viewportStartOffset).toInt()
            posX = x.toInt()
        } else {
            posY = y.toInt()
            posX = (x + viewportStartOffset).toInt()
        }
        return visibleItemsInfo
            .fastFirstOrNull { posY in it.top..it.bottom && posX in it.left..it.right }
            ?.itemKey
    }

    fun calcAutoScrollOffset(time: Long, maxScroll: Float): Float =
        selected?.let { selected ->
            val start: Int
            val size: Int
            val dist: Int
            if (isVertical) {
                start = movedDist.y + selected.top
                dist = movedDist.y
                size = selected.height
            } else {
                start = movedDist.x + selected.left
                dist = movedDist.x
                size = selected.width
            }
            val outOfBounds: Int = when {
                dist < 0 -> (start - viewportStartOffset).takeIf { it < 0 }
                dist > 0 -> (start + size - viewportEndOffset).takeIf { it > 0 }
                else -> null
            } ?: 0
            if (outOfBounds != 0) {
                interpolateOutOfBoundsScroll(size, outOfBounds.toFloat(), time, maxScroll)
            } else {
                null
            }
        } ?: 0f

    private fun findTargets(x: Int, y: Int, selected: T): List<T> {
        targets.clear()
        distances.clear()
        val left = x + selected.left
        val right = x + selected.right
        val top = y + selected.top
        val bottom = y + selected.bottom
        val centerX = (left + right) / 2
        val centerY = (top + bottom) / 2
        visibleItemsInfo.fastForEach { item ->
            if (
                item.itemIndex == draggedIndex
                || item.bottom < top
                || item.top > bottom
                || item.right < left
                || item.left > right
            ) {
                return@fastForEach
            }
            if (canDragOver?.invoke(ItemPosition(item.itemIndex, item.itemKey)) != false) {
                val dx = (centerX - (item.left + item.right) / 2).absoluteValue
                val dy = (centerY - (item.top + item.bottom) / 2).absoluteValue
                val dist = dx * dx + dy * dy
                var pos = 0
                for (j in targets.indices) {
                    if (dist > distances[j]) {
                        pos++
                    } else {
                        break
                    }
                }
                targets.add(pos, item)
                distances.add(pos, dist)
            }
        }
        return targets
    }

    private fun chooseDropItem(
        draggedItemInfo: T?,
        items: List<T>,
        curX: Int,
        curY: Int
    ): T? {
        if (draggedItemInfo == null) {
            return if (draggedIndex != null) items.lastOrNull() else null
        }
        var target: T? = null
        var highScore = -1
        val right = curX + draggedItemInfo.width
        val bottom = curY + draggedItemInfo.height
        val dx = curX - draggedItemInfo.left
        val dy = curY - draggedItemInfo.top
        items.fastForEach { item ->
            if (dx > 0) {
                val diff = item.right - right
                if (diff < 0 && item.right > draggedItemInfo.right) {
                    val score = diff.absoluteValue
                    if (score > highScore) {
                        highScore = score
                        target = item
                    }
                }
            }
            if (dx < 0) {
                val diff = item.left - curX
                if (diff > 0 && item.left < draggedItemInfo.left) {
                    val score = diff.absoluteValue
                    if (score > highScore) {
                        highScore = score
                        target = item
                    }
                }
            }
            if (dy < 0) {
                val diff = item.top - curY
                if (diff > 0 && item.top < draggedItemInfo.top) {
                    val score = diff.absoluteValue
                    if (score > highScore) {
                        highScore = score
                        target = item
                    }
                }
            }
            if (dy > 0) {
                val diff = item.bottom - bottom
                if (diff < 0 && item.bottom > draggedItemInfo.bottom) {
                    val score = diff.absoluteValue
                    if (score > highScore) {
                        highScore = score
                        target = item
                    }
                }
            }
        }
        return target
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

        private fun interpolateOutOfBoundsScroll(
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