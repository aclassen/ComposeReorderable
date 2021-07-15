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
package io.burnoutcrew.lazyreorderlist.ui.reorderlist


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.burnoutcrew.lazyreorderlist.reorderable.ReorderableState
import io.burnoutcrew.lazyreorderlist.reorderable.draggedItem
import io.burnoutcrew.lazyreorderlist.reorderable.rememberReorderState
import io.burnoutcrew.lazyreorderlist.reorderable.reorderable

@Composable
fun ReorderList(vm: ReorderListViewModel = viewModel()) {
    Column {
        HorizontalReorderList(
            items = vm.cats,
            state = rememberReorderState { from, to -> vm.moveCat(from, to) },
            modifier = Modifier.padding(vertical = 16.dp)
        )
        VerticalReorderList(
            items = vm.dogs,
            state = rememberReorderState(
                canDragOver = { vm.isDogDragEnabled(it) },
                isDragEnabled = { vm.isDogDragEnabled(it) },
                onMove = { from, to -> vm.moveDog(from, to) }))
    }
}

@Composable
fun HorizontalReorderList(
    modifier: Modifier = Modifier,
    items: SnapshotStateList<ItemData>,
    state: ReorderableState,
) {
    LazyRow(
        state = state.listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .reorderable(state, Orientation.Horizontal)
            .then(modifier),
    ) {
        itemsIndexed(items) { idx, item ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .draggedItem(if (state.index == idx) state.offset else null, Orientation.Horizontal)
                    .scale(if (state.index == null || state.index == idx) 1f else .9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colors.primary)
            ) {
                Text(item.title)
            }
        }
    }
}

@Composable
fun VerticalReorderList(
    modifier: Modifier = Modifier,
    items: SnapshotStateList<ItemData>,
    state: ReorderableState,
) {
    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .reorderable(state)
            .then(modifier)
    ) {
        itemsIndexed(items) { idx, item ->
            if (item.isLocked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                ) {
                    Text(
                        text = item.title,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .draggedItem(if (state.index == idx) state.offset else null)
                        .background(MaterialTheme.colors.surface)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Image(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = ""
                        )
                        Text(
                            text = item.title,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Divider()
                }
            }
        }
    }
}