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

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset

@Composable
fun rememberReorderLazyListState(
    listState: LazyListState = rememberLazyListState(),
    onMove: (fromIndex: ItemPosition, toIndex: ItemPosition) -> (Unit),
    canDragOver: ((index: ItemPosition) -> Boolean)? = null,
    onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
) = remember { ReorderableLazyListState(listState, onMove, canDragOver, onDragEnd) }

class ReorderableLazyListState(
    val listState: LazyListState,
    onMove: (fromIndex: ItemPosition, toIndex: ItemPosition) -> (Unit),
    canDragOver: ((index: ItemPosition) -> Boolean)? = null,
    onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
) : ReorderableState<LazyListItemInfo>(onMove, canDragOver, onDragEnd) {

    override val LazyListItemInfo.left: Int
        get() = if (isVertical) 0 else offset
    override val LazyListItemInfo.top: Int
        get() = if (isVertical) offset else 0
    override val LazyListItemInfo.right: Int
        get() = if (isVertical) 0 else offset + size
    override val LazyListItemInfo.bottom: Int
        get() = if (isVertical) offset + size else 0
    override val LazyListItemInfo.width: Int
        get() = if (isVertical) 0 else size
    override val LazyListItemInfo.height: Int
        get() = if (isVertical) size else 0
    override val LazyListItemInfo.itemIndex: Int
        get() = index
    override val LazyListItemInfo.itemKey: Any
        get() = key
    override val visibleItemsInfo: List<LazyListItemInfo>
        get() = listState.layoutInfo.visibleItemsInfo
    override val isVertical: Boolean
        get() = listState.layoutInfo.orientation == Orientation.Vertical
    override val viewportStartOffset: Int
        get() = listState.layoutInfo.viewportStartOffset
    override val viewportEndOffset: Int
        get() = listState.layoutInfo.viewportEndOffset

    override suspend fun scrollBy(value: Float): Float {
        return listState.scrollBy(value)
    }

    override suspend fun scrollToCurrentItem() {
        listState.scrollToItem(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    override val draggedOffset: IntOffset? by derivedStateOf {
        draggedIndex
            ?.let { listState.layoutInfo.itemInfoByIndex(it) }
            ?.let {
                val v = (selected?.offset ?: 0) - it.offset
                IntOffset(
                    if (isVertical) 0 else v + movedDist.x,
                    if (isVertical) v + movedDist.y else 0
                )
            }
    }

    override fun findKeyAt(x: Float, y: Float): Any? {
        return super.findKeyAt(if (isVertical) 0f else x, if (isVertical) y else 0f)
    }

    override suspend fun dragBy(x: Int, y: Int): Boolean {
        return super.dragBy(if (isVertical) 0 else x, if (isVertical) y else 0)
    }
}

private fun LazyListLayoutInfo.itemInfoByIndex(index: Int) =
    visibleItemsInfo.getOrNull(index - visibleItemsInfo.first().index)