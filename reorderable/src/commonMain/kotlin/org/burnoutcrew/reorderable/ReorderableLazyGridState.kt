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
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridLayoutInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset

@Composable
fun rememberReorderableLazyGridState(
    gridState: LazyGridState = rememberLazyGridState(),
    onMove: (fromIndex: ItemPosition, toIndex: ItemPosition) -> (Unit),
    canDragOver: ((index: ItemPosition) -> Boolean)? = null,
    onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
) = remember { ReorderableLazyGridState(gridState, onMove, canDragOver, onDragEnd) }

class ReorderableLazyGridState(
    val gridState: LazyGridState,
    onMove: (fromIndex: ItemPosition, toIndex: ItemPosition) -> (Unit),
    canDragOver: ((index: ItemPosition) -> Boolean)? = null,
    onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
) : ReorderableState<LazyGridItemInfo>(onMove, canDragOver, onDragEnd) {
    override val visibleItemsInfo: List<LazyGridItemInfo>
        get() = gridState.layoutInfo.visibleItemsInfo
    override val isVertical: Boolean
        get() = gridState.layoutInfo.orientation == Orientation.Vertical
    override val viewportStartOffset: Int
        get() = gridState.layoutInfo.viewportStartOffset
    override val viewportEndOffset: Int
        get() = gridState.layoutInfo.viewportEndOffset
    override val LazyGridItemInfo.left: Int
        get() = offset.x
    override val LazyGridItemInfo.right: Int
        get() = offset.x + size.width
    override val LazyGridItemInfo.top: Int
        get() = offset.y
    override val LazyGridItemInfo.bottom: Int
        get() = offset.y + size.height
    override val LazyGridItemInfo.width: Int
        get() = size.width
    override val LazyGridItemInfo.height: Int
        get() = size.height
    override val LazyGridItemInfo.itemIndex: Int
        get() = index
    override val LazyGridItemInfo.itemKey: Any
        get() = key
    override val draggedOffset: IntOffset? by derivedStateOf {
        draggedIndex
            ?.let { gridState.layoutInfo.itemInfoByIndex(it) }
            ?.let { (selected?.offset ?: IntOffset.Zero) + movedDist - it.offset }
    }

    override suspend fun scrollBy(value: Float): Float {
        return gridState.scrollBy(value)
    }

    override suspend fun scrollToCurrentItem() {
        gridState.scrollToItem(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset)
    }
}

private fun LazyGridLayoutInfo.itemInfoByIndex(index: Int) =
    visibleItemsInfo.getOrNull(index - visibleItemsInfo.first().index)