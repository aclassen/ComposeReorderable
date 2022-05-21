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
package org.burnoutcrew.android.ui.reorderlist


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.burnoutcrew.reorderable.ItemPosition
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.draggedItem
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable


@Composable
fun ReorderGrid(vm: ReorderListViewModel = viewModel()) {
    Column {
        HorizontalGrid(
            items = vm.cats,
            modifier = Modifier.padding(vertical = 16.dp),
            onMove = { from, to -> vm.moveCat(from, to) },
        )
        VerticalGrid(
            items = vm.dogs,
            onMove = { from, to -> vm.moveDog(from, to) },
            canDragOver = { vm.isDogDragEnabled(it) },
        )
    }
}


@Composable
private fun HorizontalGrid(
    modifier: Modifier = Modifier,
    items: List<ItemData>,
    onMove: (fromPos: ItemPosition, toPos: ItemPosition) -> (Unit),
) {
    val state = rememberReorderableLazyGridState(onMove = onMove)
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        state = state.gridState,
        modifier = modifier.reorderable(state).height(200.dp)
    ) {
        items(items, { it.key }) { item ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .draggedItem(offset = state.offsetByKey(item.key))
                    .background(MaterialTheme.colors.secondary)
                    .detectReorderAfterLongPress(state)
            ) {
                Text(item.title)
            }
        }
    }
}

@Composable
private fun VerticalGrid(
    modifier: Modifier = Modifier,
    items: List<ItemData>,
    onMove: (fromPos: ItemPosition, toPos: ItemPosition) -> (Unit),
    canDragOver: ((pos: ItemPosition) -> Boolean),
) {
    val state = rememberReorderableLazyGridState(onMove = onMove, canDragOver = canDragOver)
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        state = state.gridState,
        modifier = modifier.reorderable(state)
    ) {
        items(items, { it.key }) { item ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .padding(4.dp)
                    .draggedItem(state.offsetByKey(item.key))
                    .background(MaterialTheme.colors.primary)
                    .detectReorderAfterLongPress(state)
            ) {
                Text(item.title)
            }
        }
    }
}