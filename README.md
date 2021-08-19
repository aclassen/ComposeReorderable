[![Latest release](https://img.shields.io/github/v/release/aclassen/ComposeReorderable?color=brightgreen&label=latest%20release)](https://github.com/aclassen/ComposeReorderable/releases/latest)
# Compose LazyList reorder

A Jetpack Compose (Desktop) modifier enabling reordering in a LazyList.

![Sample](readme/sample.gif)

## Download

```
dependencies {
    implementation("org.burnoutcrew.composereorderable:reorderable:<latest_version>")
}
```

## How to use

Create `reorderState` and add the `reorderable` Modifier to the LazyList:

```
val state = rememberReorderState()

LazyColumn(
    state = state.listState,
    modifier = Modifier.reorderable(state, { from, to -> data.move(from, to) })) {
...
}
```

To make an item reorderable/draggable add at least one drag modifier to the item:

```
 Modifier.detectReorder(state)
 or
 Modifier.detectReorderAfterLongPress(state)
```

> Adding one of the detect modifiers to the LazyList instead of an item , will make all items reordable.

At least apply the dragged item offset:

```
items(items, { it.key }) {item ->
    Column(
        modifier = Modifier.draggedItem(state.offsetByKey(item.key))
    ) {
        ...
    }
}

or without keyed items:

itemsIndexed(items) { idx, item ->
    Column(
        modifier = Modifier.draggedItem(state.offsetByIndex(idx))
    ) {
        ...
    }
}
```

> You can use `draggedItem` for a default dragged effect or create your own.

Complete example:
```
@Composable
fun ReorderableList(){
    val state = rememberReorderState()
    val data = List(100) { "item $it" }.toMutableStateList()
    LazyColumn(
        state = state.listState,
        modifier = Modifier.reorderable(state, { a, b -> data.move(a, b) })
    ) {
        items(data, { it }) { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggedItem(state.offsetByKey(item))
                    .detectReorderAfterLongPress(state)
            ) {
                Text(text = item)
            }
        }
    }
}
```


## Notes

When dragging, the existing item will be modified. Because if this reason it`s important that the item must be part of the LazyList visible
items all the time.

This can be problematic if no drop target can be found during scrolling.

## License

```
Copyright 2021 André Claßen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
