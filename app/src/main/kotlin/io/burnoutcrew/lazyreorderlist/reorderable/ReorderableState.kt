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
package io.burnoutcrew.lazyreorderlist.reorderable

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*

@Composable
fun rememberReorderState(listState: LazyListState): ReorderableState =
    remember {
        ReorderableState(listState)
    }

class ReorderableState(val listState: LazyListState) {
    var position by mutableStateOf<Float?>(null)
        internal set
    var index by mutableStateOf<Int?>(null)
        internal set
    val indexWithOffset by derivedStateOf {
        index
            ?.let { listState.layoutInfo.visibleItemsInfo.getOrNull(it - listState.firstVisibleItemIndex) }
            ?.let { Pair(it.index, (position ?: 0f) - it.offset - it.size / 2f) }
    }
}