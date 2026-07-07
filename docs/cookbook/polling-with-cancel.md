# Recipe: Polling với Cancel / Generation Guard

## Khi nào dùng

Poll server status: order tracking, payment confirmation.

## Simple polling loop

`TrackOrderViewModel`:

```kotlin
pollingJob = viewModelScope.launch {
    while (true) {
        delay(15_000)
        if (terminalState) break
        fetchDetail(silent = true)
    }
}
```

## Generation guard (chống race)

```kotlin
private var fetchGeneration = 0

private fun invalidateInFlightFetches() {
    fetchGeneration++
    activeFetchJob?.cancel()
}

private fun fetchDetail() {
    val gen = fetchGeneration
    viewModelScope.launch {
        val result = repository.getDetail()
        if (gen != fetchGeneration || !isActive) return@launch
        updateState(result)
    }
}
```

Gọi `invalidateInFlightFetches()` khi:
- User confirm action
- Order id đổi
- Manual refresh override

## Payment poll (limited retries)

`CheckoutViewModel`:

```kotlin
private suspend fun pollPaymentStatus(orderId: Int): Boolean {
    repeat(5) {
        delay(3000)
        if (isPaid(orderId)) return true
    }
    return false
}
```

## Lưu ý

- `onCleared()` → cancel all jobs
- Không poll khi terminal state (CONFIRMED, CANCELLED)
- Tune interval vs pin/battery

## Tham chiếu

- [../learning/order-tracking.md](../learning/order-tracking.md)
- [../learning/checkout-payment.md](../learning/checkout-payment.md)
- [../decisions/001-socket-plus-polling.md](../decisions/001-socket-plus-polling.md)
