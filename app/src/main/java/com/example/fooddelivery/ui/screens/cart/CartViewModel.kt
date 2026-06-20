package com.example.fooddelivery.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import com.example.fooddelivery.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val isLoading: Boolean = false,
    val selectedRestaurantName: String? = null
) {
    val itemsByRestaurant: Map<String, List<CartItem>> get() = items.groupBy { it.restaurantName }
    val selectedItems: List<CartItem> get() = items.filter { it.restaurantName == selectedRestaurantName }
    val subTotal: Double get() = selectedItems.sumOf { it.totalPrice }
    val discount: Double get() = selectedVoucher?.discountAmount ?: 0.0
    val total: Double get() = (subTotal + deliveryFee - discount).coerceAtLeast(0.0)
    val isCartEmpty: Boolean get() = items.isEmpty()
    val canCheckout: Boolean get() = selectedItems.isNotEmpty() && selectedRestaurantName != null
}

sealed interface CartEvent {
    data class UpdateQuantity(val cartItemId: Int, val newQuantity: Int) : CartEvent
    data class RemoveItem(val cartItemId: Int): CartEvent
    data object ClearCart: CartEvent
    data class ApplyVoucher(val voucher: Voucher): CartEvent
    data class PromoCodeChanged(val code: String): CartEvent
    data object ApplyPromoCode: CartEvent
    data object ProceedToCheckout: CartEvent
    data class SelectRestaurant(val restaurantName: String): CartEvent
    data object SyncCart : CartEvent
}

sealed interface CartUiEffect {
    data class NavigateToCheckout(val restaurantId: String, val restaurantName: String, val discount: Double) : CartUiEffect
    data class ShowError(val message: String) : CartUiEffect
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CartUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        observeCart()
        loadMockVouchers()
        onEvent(CartEvent.SyncCart)
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                _state.update { currentState ->
                    val newSelectedName = if (items.any { it.restaurantName == currentState.selectedRestaurantName}) {
                        currentState.selectedRestaurantName
                    } else {
                        items.firstOrNull()?.restaurantName
                    }
                    currentState.copy(
                        items = items,
                        selectedRestaurantName = newSelectedName
                    )
                }
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
            is CartEvent.UpdateQuantity -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    cartRepository.updateQuantity(event.cartItemId, event.newQuantity)
                    _state.update { it.copy(isLoading = false) }
                }
            }
            is CartEvent.RemoveItem -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    cartRepository.removeItem(event.cartItemId)
                    _state.update { it.copy(isLoading = false) }
                }
            }
            is CartEvent.ClearCart -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    cartRepository.clearCart()
                    _state.update { it.copy(isLoading = false) }
                }
            }
            is CartEvent.SyncCart -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    cartRepository.syncCart()
                    _state.update { it.copy(isLoading = false) }
                }
            }
            is CartEvent.ApplyVoucher -> _state.update { it.copy(selectedVoucher = event.voucher) }
            is CartEvent.PromoCodeChanged -> _state.update { it.copy(promoCode = event.code, promoError = null) }
            is CartEvent.ApplyPromoCode -> {
                if (_state.value.promoCode.isEmpty()) {
                    _state.update { it.copy(promoError = "Invalid code") }
                }
            }
            is CartEvent.ProceedToCheckout -> {
                handleProceedToCheckout()
            }
            is CartEvent.SelectRestaurant -> {
                if (_state.value.selectedRestaurantName != event.restaurantName) {
                    _state.update {
                        it.copy(
                            selectedRestaurantName = event.restaurantName,
                            selectedVoucher = null,
                            promoCode = "",
                            promoError = null
                        )
                    }
                }
            }
        }
    }

    private fun handleProceedToCheckout() {
        val currentState = _state.value
        viewModelScope.launch {
            if (currentState.canCheckout && currentState.selectedRestaurantName != null) {
                val restaurantId = currentState.selectedItems.firstOrNull()?.restaurantId
                if (restaurantId != null) {
                    _uiEffect.emit(
                        CartUiEffect.NavigateToCheckout(
                            restaurantId = restaurantId,
                            restaurantName = currentState.selectedRestaurantName,
                            discount = currentState.discount
                        )
                    )
                } else {
                    _uiEffect.emit(CartUiEffect.ShowError("Unable to proceed to checkout"))
                }
            } else {
                val errorMsg = if (currentState.isCartEmpty) {
                    "Your cart is empty"
                } else {
                    "Please select items to checkout"
                }
                _uiEffect.emit(CartUiEffect.ShowError(errorMsg))
            }
        }
    }
}
