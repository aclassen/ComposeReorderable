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
package org.burnoutcrew.reorderable

import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastAny
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

/**
 * Replacement for the deprecated [androidx.compose.foundation.gestures.forEachGesture].
 * Use when the gesture loop needs arbitrary suspending calls (e.g. channel receive)
 * that [androidx.compose.foundation.gestures.awaitEachGesture] does not support.
 */
internal suspend fun PointerInputScope.eachGestureWhileActive(
    block: suspend PointerInputScope.() -> Unit
) {
    val currentContext = currentCoroutineContext()
    while (currentContext.isActive) {
        try {
            block()
        } catch (e: CancellationException) {
            if (!currentContext.isActive) throw e
            awaitPointerEventScope {
                awaitAllPointersUp()
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitAllPointersUp() {
    if (currentEvent.changes.fastAll { !it.pressed }) return
    do {
        awaitPointerEvent(PointerEventPass.Initial)
    } while (currentEvent.changes.fastAny { it.pressed })
}
