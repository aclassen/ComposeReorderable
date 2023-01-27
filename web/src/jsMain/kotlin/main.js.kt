import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        BrowserViewportWindow("ComposeReorderableDemo") {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    VerticalReorderList()
                }
            }
        }
    }
}


@Composable
fun VerticalReorderList() {
    val items = remember { mutableStateOf(List(100) { it }) }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        items.value = items.value.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    })
    Box {
        LazyColumn(
            state = state.listState,
            modifier = Modifier.reorderable(state)
        ) {
            items(items.value, { it }) { item ->
                ReorderableItem(state, orientationLocked = false, key = item) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp)
                    Column(
                        modifier = Modifier
                            .shadow(elevation.value)
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
                                text = item.toString(),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Divider()
                    }
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