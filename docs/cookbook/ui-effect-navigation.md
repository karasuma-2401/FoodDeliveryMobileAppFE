# Recipe: UiEffect cho Navigation & One-Shot Actions

## Khi nào dùng

- Navigate sang màn khác
- Mở app ngoài (MoMo deeplink)
- Show snackbar một lần

**Không** đặt trong `State` — tránh re-compose trigger nhiều lần.

## Code trong DFood

`CheckoutViewModel.kt`:

```kotlin
sealed interface CheckoutUiEffect {
    data class OpenMoMoApp(val deeplink: String, val total: Double) : CheckoutUiEffect
    data class NavigateToPaymentSuccessful(val orderId: Int) : CheckoutUiEffect
    data class ShowError(val message: String) : CheckoutUiEffect
}

private val _uiEffect = MutableSharedFlow<CheckoutUiEffect>()
val uiEffect = _uiEffect.asSharedFlow()

// Emit
viewModelScope.launch {
    _uiEffect.emit(CheckoutUiEffect.OpenMoMoApp(deeplink, total))
}
```

`CheckoutScreen.kt`:

```kotlin
LaunchedEffect(Unit) {
    viewModel.uiEffect.collect { effect ->
        when (effect) {
            is CheckoutUiEffect.NavigateToPaymentSuccessful ->
                onNavigateToSuccess(effect.orderId)
            ...
        }
    }
}
```

## State vs Effect

| | State | UiEffect |
|---|-------|----------|
| Ví dụ | `isLoading`, `list` | Navigate, Snackbar |
| Collect | `collectAsStateWithLifecycle` | `LaunchedEffect` + `collect` |
| Replay | Có (StateFlow) | Không (SharedFlow) |

## Template

```kotlin
private val _effect = Channel<UiEffect>(Channel.BUFFERED)
val effect = _effect.receiveAsFlow()

// Screen
LaunchedEffect(Unit) {
    viewModel.effect.collect { /* handle */ }
}
```

## Tham chiếu

- [../learning/checkout-payment.md](../learning/checkout-payment.md)
- [../learning/patterns.md](../learning/patterns.md)
