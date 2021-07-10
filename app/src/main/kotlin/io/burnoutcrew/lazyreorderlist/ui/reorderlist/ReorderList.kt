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

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            modifier = Modifier.padding(vertical = 16.dp)
        )
        VerticalReorderList(items = vm.dogs)
    }
}

@Composable
fun HorizontalReorderList(
    modifier: Modifier = Modifier,
    items: SnapshotStateList<ItemData>,
    listState: LazyListState = rememberLazyListState()
) {
    val state = rememberReorderState(listState)
    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .reorderable(state, items, Orientation.Horizontal)
            .then(modifier),
    ) {
        itemsIndexed(items) { idx, item ->
            val offset by remember {
                derivedStateOf { state.indexWithOffset?.takeIf { it.first == idx }?.second }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .draggedItem(offset, Orientation.Horizontal)
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
    listState: LazyListState = rememberLazyListState()
) {
    val state: ReorderableState = rememberReorderState(listState)
    LazyColumn(
        state = listState,
        modifier = Modifier
            .reorderable(state, items)
            .then(modifier)
    ) {
        itemsIndexed(items) { idx, item ->
            val offset by remember {
                derivedStateOf { state.indexWithOffset?.takeIf { it.first == idx }?.second }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggedItem(offset)
                    .background(MaterialTheme.colors.surface)
            ) {
                Text(
                    text = item.title,
                    modifier = Modifier.padding(24.dp)
                )
                Divider()
            }
        }
    }
}