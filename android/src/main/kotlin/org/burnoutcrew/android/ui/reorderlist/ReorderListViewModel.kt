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

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import org.burnoutcrew.reorderable.ItemPosition
import org.burnoutcrew.reorderable.move


class ReorderListViewModel : ViewModel() {
    val cats = List(500) { ItemData("Cat $it", "id$it") }.toMutableStateList()
    val dogs = List(500) {
        if (it.mod(10) == 0) ItemData("Locked", "id$it", true) else ItemData("Dog $it", "id$it")
    }.toMutableStateList()

    fun moveCat(from: ItemPosition, to: ItemPosition) {
        cats.move(from.index, to.index)
    }

    fun moveDog(from: ItemPosition, to: ItemPosition) {
        dogs.move(from.index, to.index)
    }

    fun isDogDragEnabled(pos: ItemPosition) = dogs.getOrNull(pos.index)?.isLocked != true
}
