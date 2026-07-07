# Order Tracking — Poll, ETA & Confirm Received

## 1. Bài toán

- Theo dõi trạng thái đơn realtime-ish (PENDING → … → CONFIRMED)
- Hiển thị ETA countdown **chỉ khi DELIVERING**
- Khách xác nhận đã nhận hàng khi DELIVERED
- Tránh race condition khi user refresh / confirm liên tục

## 2. Walkthrough source

| File | Vai trò |
|------|---------|
| `ui/screens/customer/order/TrackOrderScreen.kt` | Timeline UI |
| `ui/screens/customer/order/TrackOrderViewModel.kt` | Poll, ETA, confirm |
| `util/OrderEta.kt` | Parse ISO + countdown label |
| `data/repository/OrderRepositoryImpl.kt` | `getOrderDetail`, `confirmReceived` |

## 3. Tracking status mapping

```kotlin
enum class TrackingStatus(val step: Int, ...) {
    PENDING(0), PREPARING(1), DELIVERING(2),
    DELIVERED(3), CONFIRMED(4), CANCELLED(-1)
}
```

`resolveTrackingStatus` — **không lùi step** (monotonic): `max(current.step, incoming.step)` trừ CANCELLED.

## 4. Polling

```kotlin
private fun startPolling(orderId: String) {
    pollingJob = viewModelScope.launch {
        while (true) {
            delay(15_000)
            if (confirmed || cancelled || isConfirming) break
            fetchOrderDetail(orderId, isUserRefresh = false)
        }
    }
}
```

**Constant:** poll mỗi **15 giây**

## 5. Fetch generation guard

```kotlin
private var fetchGeneration = 0

private fun invalidateInFlightFetches() {
    fetchGeneration++
    activeFetchJob?.cancel()
}

// Trong fetchOrderDetail:
val generation = fetchGeneration
val result = orderRepository.getOrderDetail(id)
if (generation != fetchGeneration || !isActive) return@launch
```

→ User confirm hoặc refresh không bị response cũ ghi đè state mới.

**Cookbook:** [../cookbook/polling-with-cancel.md](../cookbook/polling-with-cancel.md)

## 6. ETA logic

Theo `docs/config.md` (BE contract):

| Status | Hiển thị |
|--------|----------|
| PENDING / PREPARING | Chỉ text trạng thái — **không** countdown |
| DELIVERING | `expected_arrival` + countdown |
| DELIVERED | `delivered_at` |
| CONFIRMED | Hoàn tất |

`OrderEta.countdownLabel(iso)`:
- `<= 0` → "Taking a bit longer than expected"
- `< 1 min` → "Arriving soon"
- else → "~X min left"

Countdown ticker refresh mỗi **30 giây** (`syncCountdownTicker`).

## 7. Confirm received

```kotlin
// Chỉ khi DELIVERED và chưa confirm
orderRepository.confirmReceived(orderId)
// Handle "already confirmed" như success
syncConfirmedOrderFromServer()
stopPolling()
```

`confirmInFlight` flag chống double-tap.

## 8. API docs

`docs/OrderFlow.md`, `docs/config.md` (ETA fields)

## 9. Demo scenarios

```
Scenario: ETA
1. Order PREPARING → không thấy countdown
2. NH chuyển DELIVERING → countdown xuất hiện
3. DELIVERED → hiện thời gian giao

Scenario: Confirm race
1. User bấm Confirm
2. invalidateInFlightFetches — poll cũ cancel
3. confirmInFlight block double tap
```

## 10. Nếu làm lại từ đầu

1. `TrackingStatus` enum + timeline UI component
2. `fetchOrderDetail` + generation counter
3. `startPolling` / `stopPolling` lifecycle
4. `OrderEta` utility + unit test
5. Confirm action + optimistic UI confirming state

## 11. Tham chiếu

- [checkout-payment.md](checkout-payment.md)
- [chat.md](chat.md) — mở chat từ order (`orderId`, `sellerId`)
