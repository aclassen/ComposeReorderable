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
package org.burnoutcrew.reorderable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex

fun Modifier.draggedItem(offset: IntOffset?): Modifier = this.then(
    zIndex(offset?.let { 1f } ?: 0f)
        .graphicsLayer {
            translationX = offset?.x?.toFloat() ?: 0f
            translationY = offset?.y?.toFloat() ?: 0f
            shadowElevation = offset?.let { 8f } ?: 0f
        }
)