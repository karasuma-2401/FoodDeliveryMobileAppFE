package com.example.fooddelivery.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import com.example.fooddelivery.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartState(
    val items: List<CartItem> = emptyList(),
    val deliveryFee: Double = 5.0,
    val availableVouchers: List<Voucher> = emptyList(),
    val selectedVoucher: Voucher? = null,
    val promoCode: String = "",
    val promoError: String? = null,
    val isLoading: Boolean = false
) {
    val subTotal: Double get() = items.sumOf { it.totalPrice }
    val discount: Double get() = selectedVoucher?.discountAmount ?: 0.0
    val total: Double get() = (subTotal + deliveryFee - discount).coerceAtLeast(0.0)
    val isCartEmpty: Boolean get() = items.isEmpty()
}

sealed interface CartEvent {
    data class UpdateQuantity(val foodId: String, val size: String, val delta: Int) : CartEvent
    data class RemoveItem(val foodId: String, val size: String): CartEvent
    data object ClearCart: CartEvent
    data class ApplyVoucher(val voucher: Voucher): CartEvent
    data class PromoCodeChanged(val code: String): CartEvent
    data object ApplyPromoCode: CartEvent
    data object ProceedToCheckout: CartEvent
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    init {
        observeCart()
        loadMockVouchers()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.getCartItems().collectLatest { items ->
                _state.update { it.copy(items = items) }
            }
        }
    }

    private fun loadMockVouchers() {
        val mockVouchers = listOf(
            Voucher(
                id = "1",
                code = "SALE20",
                title = "Giảm 20% tối đa $15",
                description = "Cho đơn hàng từ $50",
                discountAmount = 15.0,
                minOrderAmount = 50.0,
                expiryText = "Hết hạn trong 2 ngày",
                type = VoucherType.DISCOUNT,
                isApplicable = true
            ),
            Voucher(
                id = "2",
                code = "FREESHIP",
                title = "Free Ship tối đa $5",
                description = "Cho đơn hàng từ $100",
                discountAmount = 5.0,
                minOrderAmount = 100.0,
                type = VoucherType.FREESHIP,
                isApplicable = false,
                conditionMessage = "Mua thêm $12 nữa để áp dụng mã này"
            )
        )
        _state.update { it.copy(availableVouchers = mockVouchers) }
    }

    fun onEvent(event: CartEvent) {
        when (event) {
            is CartEvent.UpdateQuantity -> cartRepository.updateQuantity(event.foodId, event.size, event.delta)
            is CartEvent.RemoveItem -> cartRepository.removeItem(event.foodId, event.size)
            is CartEvent.ClearCart -> cartRepository.clearCart()
            is CartEvent.ApplyVoucher -> _state.update { it.copy(selectedVoucher = event.voucher) }
            is CartEvent.PromoCodeChanged -> _state.update { it.copy(promoCode = event.code, promoError = null) }
            is CartEvent.ApplyPromoCode -> {
                if (_state.value.promoCode.isEmpty()) {
                    _state.update { it.copy(promoError = "Invalid code") }
                }
            }
            is CartEvent.ProceedToCheckout -> { /* Handle checkout */ }
        }
    }
}
