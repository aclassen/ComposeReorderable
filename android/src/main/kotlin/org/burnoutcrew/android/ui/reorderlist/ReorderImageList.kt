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


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import org.burnoutcrew.android.R
import org.burnoutcrew.reorderable.*

@Composable
fun ReorderImageList(
    vm: ImageListViewModel = viewModel(),
    state: ReorderableState = rememberReorderState(),
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = state.listState,
        modifier = modifier
            .then(
                Modifier.reorderable(
                    state,
                    onMove = { from, to -> vm.onMove(from, to) },
                    canDragOver = { vm.canDragOver(it) })
            )
    ) {
        item {
            HeaderFooter(stringResource(R.string.header_title), vm.headerImage)
        }
        items(vm.images, { it }) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggedItem(state.offsetByKey(item))
                    .background(MaterialTheme.colors.surface)
                    .detectReorderAfterLongPress(state)
            ) {
                Row {
                    Image(
                        painter = rememberImagePainter(item),
                        contentDescription = null,
                        modifier = Modifier.size(128.dp)
                    )
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Divider()
            }
        }
        item {
            HeaderFooter(stringResource(R.string.footer_title), vm.footerImage)
        }
    }
}

@Composable
private fun HeaderFooter(title: String, url: String) {
    Box(modifier = Modifier.height(128.dp).fillMaxWidth()) {
        Image(
            painter = rememberImagePainter(url),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Text(
            title,
            style = MaterialTheme.typography.h2,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}