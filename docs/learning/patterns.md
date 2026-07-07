# Pattern Catalog — Tái sử dụng trong DFood & project khác

Catalog các pattern lặp lại trong codebase. Mỗi pattern có: **khi nào dùng**, **file tham chiếu**, **link cookbook**.

---

## 1. State + Event + UiEffect (Unidirectional Data Flow)

**Khi nào:** Mọi màn hình có user interaction.

| Thành phần | Vai trò |
|------------|---------|
| `State` | Data UI render (loading, list, error message) |
| `Event` | User action (`onClick`, `onTextChange`) |
| `UiEffect` | One-shot: navigate, open MoMo, snackbar |

**File mẫu:** `CheckoutViewModel.kt`, `TrackOrderViewModel.kt`, `HomeViewModel.kt`

**Cookbook:** [../cookbook/ui-effect-navigation.md](../cookbook/ui-effect-navigation.md)

---

## 2. Optimistic UI + Rollback

**Khi nào:** Action cần phản hồi tức thì (cart, chat send).

**Flow:**
1. Ghi local ngay (Room) với flag `isSending` / tăng quantity
2. Gọi API / Socket
3. Fail → revert từ snapshot

**File mẫu:**
- Cart: `CartRepositoryImpl.addToCartWithOptimisticLocal`
- Chat: `ChatRepositoryImpl.sendMessage` + `handleNewMessage` dedup

**Cookbook:** [../cookbook/optimistic-ui-rollback.md](../cookbook/optimistic-ui-rollback.md)

**ADR:** [../decisions/003-optimistic-ui.md](../decisions/003-optimistic-ui.md)

---

## 3. Server-as-Source-of-Truth Sync

**Khi nào:** Sau mỗi mutation API thành công, replace local bằng response server.

**File mẫu:** `CartRepositoryImpl.applyServerCart` — `clearCart()` + insert all items từ `CartResponse`

**ADR:** [../decisions/005-server-as-cart-source-of-truth.md](../decisions/005-server-as-cart-source-of-truth.md)

---

## 4. Flow observe + collectLatest

**Khi nào:** UI reactive từ Room hoặc Repository Flow.

```kotlin
viewModelScope.launch {
    repository.getItems().collectLatest { items ->
        _state.update { it.copy(items = items) }
    }
}
```

**File mẫu:** `ChatViewModel.observeMessages`, `CartViewModel`, badge cart trên `HomeViewModel`

**Cookbook:** [../cookbook/room-flow-compose.md](../cookbook/room-flow-compose.md)

---

## 5. Debounce + Job Cancel

**Khi nào:** Search, autocomplete — tránh spam API mỗi keystroke.

**File mẫu:** `SearchViewModel` — `searchJob?.cancel()` + `delay(500L)`

**Cookbook:** [../cookbook/debounce-search.md](../cookbook/debounce-search.md)

---

## 6. Polling với Cancel / Generation

**Khi nào:** Trạng thái thay đổi trên server, không có push reliable (order status, MoMo payment).

**Biến thể:**
- **Simple loop:** `TrackOrderViewModel.startPolling` — delay 15s
- **Generation guard:** `fetchGeneration++` — invalidate fetch cũ khi user refresh/confirm
- **Hybrid:** Chat socket + poll 8s fallback

**Cookbook:** [../cookbook/polling-with-cancel.md](../cookbook/polling-with-cancel.md)

**ADR:** [../decisions/001-socket-plus-polling.md](../decisions/001-socket-plus-polling.md)

---

## 7. Token Refresh Chain

**Khi nào:** Access token hết hạn — silent refresh, không bắt user login lại.

**Flow:** `getMe` → 401 → `refreshToken` → save tokens → `chatSocketManager.reconnectWithCurrentToken()` → retry `getMe`

**File mẫu:** `ValidateSessionUseCase.kt`

**ADR:** [../decisions/004-token-refresh-chain.md](../decisions/004-token-refresh-chain.md)

---

## 8. Shared Singleton State (Delivery Location)

**Khi nào:** Nhiều màn hình cùng đọc/ghi một context (địa chỉ giao hiện tại).

**File mẫu:** `DeliveryLocationRepositoryImpl` — `@Singleton` + `StateFlow<DeliveryLocationState>`

Chi tiết: [delivery-location.md](delivery-location.md)

---

## 9. Parallel Fetch với Semaphore + Cache

**Khi nào:** Enrich list (N items) cần N API calls — giới hạn concurrency.

**File mẫu:** `EnrichRestaurantsWithVoucherBadgesUseCase` — `Semaphore(5)` + `mutableMapOf` cache

---

## 10. Offline-First Read

**Khi nào:** List data — API fail vẫn hiện được từ cache.

**File mẫu:** `NotificationRepositoryImpl.getNotificationsPaged` — try API → insert Room; catch → read Room

**ADR:** [../decisions/002-room-cache-strategy.md](../decisions/002-room-cache-strategy.md)

---

## 11. Pagination Infinite Scroll

**Khi nào:** Danh sách dài (notifications, chat history).

**Pattern:** `page` + `pageSize`, `isEndReached = list.size < pageSize`, trigger load more khi scroll gần cuối.

**File mẫu:** `NotificationViewModel`, `ChatViewModel.loadMoreMessages`

**Cookbook:** [../cookbook/pagination-infinite-scroll.md](../cookbook/pagination-infinite-scroll.md)

---

## 12. Badge ViewModel ở Graph Level

**Khi nào:** Bottom bar badge (notification unread) — scope rộng hơn 1 screen.

**File mẫu:** `NavGraph.kt` — `hiltViewModel<UnreadNotificationViewModel>()`, refresh on `ON_RESUME`

---

## 13. Fly-to-Cart Animation State

**Khi nào:** Micro-interaction UX khi add to cart.

**File mẫu:** `FlyToCartState.kt`, `FlyToCartOverlay.kt` — shared `@Stable` state, `rememberFlyToCartState()`

---

## 14. Result<T> Error Handling

**Khi nào:** Repository layer — không throw ra UI trực tiếp.

```kotlin
return try {
    api.getX().unwrapData("msg").mapCatching { ... }
} catch (e: Exception) {
  if (e is CancellationException) throw e
    Result.failure(e)
}
```

**File mẫu:** Hầu hết `*RepositoryImpl.kt`

---

## Ma trận: Pattern × Feature

| Feature | Patterns dùng |
|---------|---------------|
| Auth | Token refresh chain, side effect socket reconnect |
| Cart | Optimistic UI, server sync |
| Checkout | State/Event/Effect, polling payment |
| Track Order | Polling + generation, countdown ticker |
| Chat | Optimistic, socket+polling, Flow observe |
| Notification | Offline-first, pagination |
| Search | Debounce, shared location observe |
| Home | Parallel enrich, multiple Flow observers |
