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

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun ReorderList(vm: ReorderListViewModel = viewModel()) {
    Column {
        HorizontalReorderList(
            vm = vm,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        VerticalReorderList(vm = vm)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VerticalReorderList(
    modifier: Modifier = Modifier,
    vm: ReorderListViewModel,
) {
    val state = rememberReorderableLazyListState(onMove = vm::moveDog, canDragOver = vm::isDogDragEnabled)
    val context = LocalContext.current
    LazyColumn(
        state = state.listState,
        modifier = modifier.reorderable(state)
    ) {

        items(vm.dogs, { item -> item.key }) { item ->
            ReorderableItem(state, item.key) { dragging ->
                val elevation = animateDpAsState(if (dragging) 8.dp else 0.dp)
                val interactionSource = remember {
                    MutableInteractionSource()
                }
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
                            .detectReorderAfterLongPress(state)
                            .shadow(elevation.value)
                            .fillMaxWidth()
                            .background(MaterialTheme.colors.surface)
                    ) {
                        Text(
                            text = item.title,
                            modifier = Modifier.padding(16.dp)
                                .combinedClickable(
                                    interactionSource = interactionSource,
                                    indication = rememberRipple(false),
                                    onLongClick = {
                                        Toast.makeText(context, "Thi will stop the detect of parent view", Toast.LENGTH_SHORT).show()
                                    },
                                    onClick = {}
                                )
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalReorderList(
    vm: ReorderListViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberReorderableLazyListState(onMove = vm::moveCat)
    LazyRow(
        state = state.listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .then(Modifier.reorderable(state))
            .detectReorderAfterLongPress(state),
    ) {
        items(vm.cats, { item -> item.key }) { item ->
            ReorderableItem(state, item.key) { dragging ->
                val scale = animateFloatAsState(if (dragging) 1.1f else 1.0f)
                val elevation = if (dragging) 8.dp else 0.dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale.value)
                        .shadow(elevation, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Red)
                ) {
                    Text(item.title)
                }
            }
        }
    }
}
