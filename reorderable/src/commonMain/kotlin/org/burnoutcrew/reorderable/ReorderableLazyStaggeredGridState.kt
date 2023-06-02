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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemInfo
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberReorderableLazyHorizontalStaggeredGridState(
    onMove: (ItemPosition, ItemPosition) -> Unit,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    canDragOver: ((draggedOver: ItemPosition, dragging: ItemPosition) -> Boolean)? = null,
    onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
    maxScrollPerFrame: Dp = 20.dp,
    dragCancelledAnimation: DragCancelledAnimation = SpringDragCancelledAnimation(),
) = rememberReorderableLazyStaggeredGridState(
    onMove = onMove,
    gridState = gridState,
    canDragOver = canDragOver,
    onDragEnd = onDragEnd,
    maxScrollPerFrame = maxScrollPerFrame,
    dragCancelledAnimation = dragCancelledAnimation,
    orientation = Orientation.Horizontal
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberReorderableLazyVerticalStaggeredGridState(
    onMove: (ItemPosition, ItemPosition) -> Unit,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    canDragOver: ((draggedOver: ItemPosition, dragging: ItemPosition) -> Boolean)? = null,
    onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
    maxScrollPerFrame: Dp = 20.dp,
    dragCancelledAnimation: DragCancelledAnimation = SpringDragCancelledAnimation(),
) = rememberReorderableLazyStaggeredGridState(
    onMove = onMove,
    gridState = gridState,
    canDragOver = canDragOver,
    onDragEnd = onDragEnd,
    maxScrollPerFrame = maxScrollPerFrame,
    dragCancelledAnimation = dragCancelledAnimation,
    orientation = Orientation.Vertical
)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberReorderableLazyStaggeredGridState(
    onMove: (ItemPosition, ItemPosition) -> Unit,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    canDragOver: ((draggedOver: ItemPosition, dragging: ItemPosition) -> Boolean)? = null,
    onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
    maxScrollPerFrame: Dp = 20.dp,
    dragCancelledAnimation: DragCancelledAnimation = SpringDragCancelledAnimation(),
    orientation: Orientation
): ReorderableLazyStaggeredGridState {
    val maxScroll = with(LocalDensity.current) { maxScrollPerFrame.toPx() }
    val scope = rememberCoroutineScope()
    val state = remember(gridState) {
        ReorderableLazyStaggeredGridState(
            gridState,
            scope,
            maxScroll,
            onMove,
            canDragOver,
            onDragEnd,
            dragCancelledAnimation,
            orientation = orientation
        )
    }
    LaunchedEffect(state) {
        state.visibleItemsChanged()
            .collect { state.onDrag(0, 0) }
    }

    LaunchedEffect(state) {
        while (true) {
            val diff = state.scrollChannel.receive()
            gridState.scrollBy(diff)
        }
    }
    return state
}

@OptIn(ExperimentalFoundationApi::class)
class ReorderableLazyStaggeredGridState(
    val gridState: LazyStaggeredGridState,
    scope: CoroutineScope,
    maxScrollPerFrame: Float,
    onMove: (fromIndex: ItemPosition, toIndex: ItemPosition) -> (Unit),
    canDragOver: ((draggedOver: ItemPosition, dragging: ItemPosition) -> Boolean)? = null,
    onDragEnd: ((startIndex: Int, endIndex: Int) -> (Unit))? = null,
    dragCancelledAnimation: DragCancelledAnimation = SpringDragCancelledAnimation(),
    val orientation: Orientation
) : ReorderableState<LazyStaggeredGridItemInfo>(
    scope,
    maxScrollPerFrame,
    onMove,
    canDragOver,
    onDragEnd,
    dragCancelledAnimation
) {
    override val isVerticalScroll: Boolean
        get() = orientation == Orientation.Vertical // XXX gridState.isVertical is not accessible
    override val LazyStaggeredGridItemInfo.left: Int
        get() = offset.x
    override val LazyStaggeredGridItemInfo.right: Int
        get() = offset.x + size.width
    override val LazyStaggeredGridItemInfo.top: Int
        get() = offset.y
    override val LazyStaggeredGridItemInfo.bottom: Int
        get() = offset.y + size.height
    override val LazyStaggeredGridItemInfo.width: Int
        get() = size.width
    override val LazyStaggeredGridItemInfo.height: Int
        get() = size.height
    override val LazyStaggeredGridItemInfo.itemIndex: Int
        get() = index
    override val LazyStaggeredGridItemInfo.itemKey: Any
        get() = key
    override val visibleItemsInfo: List<LazyStaggeredGridItemInfo>
        get() = gridState.layoutInfo.visibleItemsInfo
    override val viewportStartOffset: Int
        get() = gridState.layoutInfo.viewportStartOffset
    override val viewportEndOffset: Int
        get() = gridState.layoutInfo.viewportEndOffset
    override val firstVisibleItemIndex: Int
        get() = gridState.firstVisibleItemIndex
    override val firstVisibleItemScrollOffset: Int
        get() = gridState.firstVisibleItemScrollOffset

    override suspend fun scrollToItem(index: Int, offset: Int) {
        gridState.scrollToItem(index, offset)
    }
}
