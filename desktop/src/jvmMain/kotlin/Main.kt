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

import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.burnoutcrew.reorderable.*

fun main() = application {
    val data = List(500) { "Cat $it" }.toMutableStateList()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Lazy reorder list"
    ) {
        VerticalReorderList(items = data) { a, b -> data.move(a.index, b.index) }
    }
}

@Composable
fun VerticalReorderList(
    items: List<String>,
    state: ReorderableState = rememberReorderState(),
    onMove: (fromPos: ItemPosition, toPos: ItemPosition) -> (Unit),
) {
    Box {
        LazyColumn(
            state = state.listState,
            modifier = Modifier.reorderable(state, onMove)) {
            items(items, { it }) { item ->
                Column(
                    modifier = Modifier.draggedItem(state.offsetByKey(item))
                        .background(MaterialTheme.colors.surface)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Image(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "",
                            modifier = Modifier.detectReorder(state)
                        )
                        Text(
                            text = item,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Divider()
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(state.listState)
        )
    }
}