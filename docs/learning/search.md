# Search — Debounce, Filter & History

## 1. Bài toán

- Tìm nhà hàng/món theo keyword
- Không gọi API mỗi ký tự — debounce
- Lưu lịch sử tìm kiếm, trending keywords
- Kết quả phụ thuộc vị trí giao (lat/lng)
- Filter sort + category

## 2. Walkthrough source

| File | Vai trò |
|------|---------|
| `ui/screens/customer/search/SearchScreen.kt` | UI |
| `ui/screens/customer/search/SearchViewModel.kt` | Logic chính |
| `data/repository/SearchRepositoryImpl.kt` | API + history |
| `data/remote/api/SearchApi.kt` | Endpoints |

## 3. Debounce pattern

```kotlin
// SearchViewModel — QueryChanged
searchJob?.cancel()
searchJob = viewModelScope.launch {
    if (event.query.isBlank()) {
        loadInitialData()
    } else {
        delay(500L)
        performSearch()
    }
}
```

**Constants:** `500 ms` debounce

**Cookbook:** [../cookbook/debounce-search.md](../cookbook/debounce-search.md)

## 4. Observers trong init

| Observer | Mục đích |
|----------|----------|
| `deliveryLocationRepository` | lat/lng cho search |
| `cartRepository.cartItems` | Badge cart trên search bar |
| `chatRepository.getConversations()` | Badge tin nhắn |
| `selectedAddressId` change | Reload search / initial data |

## 5. Search state

```kotlin
data class SearchState(
    val searchQuery: String,
    val recentKeyWords: List<SearchHistory>,
    val trendingKeywords: List<TrendingKeyword>,
    val suggestedRestaurants: List<Restaurant>,
    val popularFood: List<FoodItem>,
    val selectedSort: SearchSortOption?,
    val selectedCategoryId: String?,
    val lat: Double?, val lng: Double?,
    ...
)
```

## 6. performSearch()

Gọi `searchRepository.search(...)` với query + lat/lng + sort + category.

Empty state: `components/EmptySearchView.kt`

## 7. History management

| Event | Action |
|-------|--------|
| `DeleteHistoryItem` | API delete + filter local list |
| `ClearAllHistory` | API clear + empty local |

## 8. API

Xem `docs/Search.md`

## 9. Demo scenarios

```
Scenario: Gõ nhanh "pho"
1. Cancel job cũ mỗi keystroke
2. Chỉ 1 API call sau 500ms từ ký tự cuối

Scenario: Đổi địa chỉ khi đang search
1. selectedAddressId đổi
2. performSearch() lại với lat/lng mới
```

## 10. Nếu làm lại từ đầu

1. `SearchRepository` + API
2. `SearchViewModel` với `searchJob` nullable
3. Wire `deliveryLocation` observe
4. `SearchScreen` + `SearchInputField`

## 11. Tham chiếu

- [delivery-location.md](delivery-location.md)
- [home.md](home.md)
