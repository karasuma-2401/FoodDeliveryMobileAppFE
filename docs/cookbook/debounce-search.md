# Recipe: Debounce Search

## Khi nào dùng

User gõ text liên tục — chỉ gọi API sau khi **ngừng gõ** một khoảng (search, filter autocomplete).

## Code trong DFood

`SearchViewModel.kt`:

```kotlin
private var searchJob: Job? = null

is SearchEvent.QueryChanged -> {
    _state.update { it.copy(searchQuery = event.query) }
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
        if (event.query.isBlank()) {
            loadInitialData()
        } else {
            delay(500L)  // debounce
            performSearch()
        }
    }
}
```

## Template copy sang project mới

```kotlin
private var debounceJob: Job? = null

fun onQueryChange(query: String) {
    debounceJob?.cancel()
    debounceJob = viewModelScope.launch {
        delay(300L) // tune: 300-500ms
        if (query.isBlank()) return@launch
        repository.search(query)
    }
}
```

## Lưu ý

- Luôn `cancel()` job cũ trước khi tạo mới
- Blank query → reset UI, không gọi API
- `CancellationException` không phải lỗi — đừng show error snackbar

## Tham chiếu

- [../learning/search.md](../learning/search.md)
