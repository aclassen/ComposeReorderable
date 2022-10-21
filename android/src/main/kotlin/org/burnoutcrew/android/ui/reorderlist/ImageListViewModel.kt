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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.burnoutcrew.reorderable.ItemPosition
import kotlin.random.Random


class ImageListViewModel : ViewModel() {
    val headerImage = "https://picsum.photos/seed/compose${Random.nextInt(Int.MAX_VALUE)}/400/200"
    val footerImage = "https://picsum.photos/seed/compose${Random.nextInt(Int.MAX_VALUE)}/400/200"
    var images by mutableStateOf(List(20) { "https://picsum.photos/seed/compose$it/200/300" })
    fun onMove(from: ItemPosition, to: ItemPosition) {
        images = images.toMutableList().apply {
            add(images.indexOfFirst { it == to.key }, removeAt(images.indexOfFirst { it == from.key }))
        }
    }

    fun canDragOver(draggedOver: ItemPosition, dragging: ItemPosition) = images.any { it == draggedOver.key }
}