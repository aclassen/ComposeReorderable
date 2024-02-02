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
package org.burnoutcrew.android.ui.reorderlist

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.rememberReorderableLazyVerticalStaggeredGridState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun ReorderGrid(vm: ReorderListViewModel = viewModel()) {
    Column {
        HorizontalGrid(
            vm = vm,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        VerticalGrid(vm = vm, modifier = Modifier.padding(vertical = 16.dp))
        VerticalStaggeredGrid(vm = vm, modifier = Modifier.padding(vertical = 16.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VerticalStaggeredGrid(
    vm: ReorderListViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberReorderableLazyVerticalStaggeredGridState(
        onMove = vm::moveDog,
        canDragOver = vm::isDogDragEnabled
    )
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(4),
        state = state.gridState,
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalItemSpacing = 4.dp,
        modifier = modifier.reorderable(state),
    ) {
        items(items = vm.dogs, key = { it.key }) { item ->
            if (item.isLocked) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colors.surface)
                ) {
                    Text(item.title)
                }
            } else {
                ReorderableItem(state, item.key) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .detectReorder(state)
                            .shadow(elevation.value)
                            .aspectRatio(1f)
                            .background(MaterialTheme.colors.primary)
                    ) {
                        Text(item.title)
                    }
                }
            }
        }
    }
}


@Composable
private fun HorizontalGrid(
    vm: ReorderListViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberReorderableLazyGridState(onMove = vm::moveCat)
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        state = state.gridState,
        contentPadding = PaddingValues(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .reorderable(state)
            .height(200.dp)
            .detectReorderAfterLongPress(state)
    ) {
        items(vm.cats, { it.key }) { item ->
            ReorderableItem(state, item.key) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .shadow(elevation.value)
                        .aspectRatio(1f)
                        .background(MaterialTheme.colors.secondary)
                ) {
                    Text(item.title)
                }
            }
        }
    }
}

@Composable
private fun VerticalGrid(
    vm: ReorderListViewModel,
    modifier: Modifier = Modifier,
) {
    val state =
        rememberReorderableLazyGridState(onMove = vm::moveDog, canDragOver = vm::isDogDragEnabled)
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        state = state.gridState,
        contentPadding = PaddingValues(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.reorderable(state)
    ) {
        items(vm.dogs, { it.key }) { item ->
            if (item.isLocked) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colors.surface)
                ) {
                    Text(item.title)
                }
            } else {
                ReorderableItem(state, item.key) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .detectReorderAfterLongPress(state)
                            .shadow(elevation.value)
                            .aspectRatio(1f)
                            .background(MaterialTheme.colors.primary)
                    ) {
                        Text(item.title)
                    }
                }
            }
        }
    }
}
