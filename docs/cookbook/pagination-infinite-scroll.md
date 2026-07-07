# Recipe: Pagination Infinite Scroll

## Khi nào dùng

List dài: notifications, chat history, orders.

## ViewModel state

```kotlin
data class ListState(
    val items: List<Item> = emptyList(),
    val page: Int = 1,
    val isPaginating: Boolean = false,
    val isEndReached: Boolean = false,
)
```

## Load more trigger (Compose)

```kotlin
val shouldLoadMore = remember {
    derivedStateOf {
        val last = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
        last.index >= listState.layoutInfo.totalItemsCount - 2
    }
}

LaunchedEffect(shouldLoadMore.value) {
    if (shouldLoadMore.value && !state.isPaginating && !state.isEndReached) {
        viewModel.onEvent(LoadMore)
    }
}
```

## End reached logic

```kotlin
result.onSuccess { list ->
    _state.update {
        it.copy(
            items = current.items + list,
            page = nextPage,
            isEndReached = list.size < pageSize
        )
    }
}
```

## DFood examples

| Feature | pageSize |
|---------|----------|
| Notification | 10 |
| Chat history | 20 (offset = page * 20) |

## Tham chiếu

- [../learning/notification.md](../learning/notification.md)
- [../learning/chat.md](../learning/chat.md)
