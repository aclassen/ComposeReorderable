# Compose LazyList reorder
[![](https://jitpack.io/v/aclassen/ComposeReorderable.svg)](https://jitpack.io/#aclassen/ComposeReorderable)

A Jetpack Compose modifier enabling reordering in a LazyList.

![Sample](readme/sample.gif)

## Download

```
repositories {
    maven { setUrl("https://jitpack.io") }
    // maven { url 'https://jitpack.io' } 
}


dependencies {
    implementation("com.github.aclassen:ComposeReorderable:<latest_version>")
}
```


## How to use

Add a `Reorderable` to your composition:

```
val state: ReorderableState = rememberReorderState()

Reorderable(state, { from, to -> data.move(from, to) })
LazyColumn(state = state.listState) {
...
}
```

To apply the dragged item offset:

```
items(items, { it.key }) {item ->
    Column(
        modifier = Modifier.draggedItem(state.offsetOf(item.key))
    ) {
        ...
    }
}
```

Make an item reorderable by adding at least one drag modifier to the item:

```
 Modifier.detectReorder(state, { item.key })
 or
 Modifier.detectReorderAfterLongPress(state, { item.key })
```


If you want to use a non keyed item list `detectReorder` and `detectReorderAfterLongPress` will not work , use the `detectListReorder` modifier instead.

Add this modifier to your LazyList , this will make the items reorderable after long press.

```
Reorderable(state, { from, to -> data.move(from, to) })
LazyRow(
    state = state.listState,
    modifier = Modifier
        .detectListReorder(state),
    ) 
```

Use `draggedItem` for a default dragged effect or create your own.

## Notes
When dragging, the existing item will be modified.
Because if this reason it`s important that the item must be part of the LazyList visible items all the time.

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
