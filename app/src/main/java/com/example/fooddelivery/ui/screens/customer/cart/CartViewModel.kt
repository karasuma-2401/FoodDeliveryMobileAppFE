package com.example.fooddelivery.ui.screens.customer.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.remote.dto.toDomain
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.VoucherRepository
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
    val availableVouchers: List<Voucher> = emptyList(),
    val selectedVoucher: Voucher? = null,
    val promoCode: String = "",
    val promoError: String? = null,
    val isLoading: Boolean = false,
    val selectedRestaurantName: String? = null,
    val discount: Double = 0.0
) {
    val itemsByRestaurant: Map<String, List<CartItem>> get() = items.groupBy { it.restaurantName }
    val selectedItems: List<CartItem> get() = items.filter { it.restaurantName == selectedRestaurantName }
    val subTotal: Double get() = selectedItems.sumOf { it.totalPrice }
    val total: Double get() = (subTotal - discount).coerceAtLeast(0.0)
    val isCartEmpty: Boolean get() = items.isEmpty()
    val canCheckout: Boolean get() = selectedItems.isNotEmpty() && selectedRestaurantName != null
}

sealed interface CartEvent {
    data class UpdateQuantity(val cartItemId: Int, val newQuantity: Int) : CartEvent
    data class RemoveItem(val cartItemId: Int) : CartEvent
    data object ClearCart : CartEvent
    data class ApplyVoucher(val voucher: Voucher) : CartEvent
    data class PromoCodeChanged(val code: String) : CartEvent
    data object ApplyPromoCode : CartEvent
    data object ProceedToCheckout : CartEvent
    data class SelectRestaurant(val restaurantName: String) : CartEvent
    data object SyncCart : CartEvent
}

sealed interface CartUiEffect {
    data class NavigateToCheckout(
        val restaurantId: String,
        val restaurantName: String,
        val discount: Double,
        val voucherId: Int? = null
    ) : CartUiEffect
    data class ShowError(val message: String) : CartUiEffect
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val voucherRepository: VoucherRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CartUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        observeCart()
        onEvent(CartEvent.SyncCart)
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                _state.update { currentState ->
                    val newSelectedName = if (items.any { it.restaurantName == currentState.selectedRestaurantName }) {
                        currentState.selectedRestaurantName
                    } else {
                        items.firstOrNull()?.restaurantName
                    }
                    currentState.copy(
                        items = items,
                        selectedRestaurantName = newSelectedName
                    )
                }
                loadSuitableVouchers()
            }
        }
    }

    private fun loadSuitableVouchers() {
        val currentState = _state.value
        val restaurantId = currentState.selectedItems.firstOrNull()?.restaurantId?.toIntOrNull() ?: run {
            _state.update { it.copy(availableVouchers = emptyList(), selectedVoucher = null, discount = 0.0) }
            return
        }
        val subtotal = currentState.subTotal

        viewModelScope.launch {
            voucherRepository.getSuitableVouchers(restaurantId, subtotal)
                .onSuccess { vouchersDto ->
                    val domainVouchers = vouchersDto.map { it.toDomain() }
                    _state.update { state ->
                        val updatedSelected = domainVouchers.find { it.id == state.selectedVoucher?.id }
                        val newDiscount = updatedSelected?.let { calculateDiscount(it, state.subTotal) } ?: 0.0
                        state.copy(
                            availableVouchers = domainVouchers,
                            selectedVoucher = updatedSelected,
                            discount = newDiscount
                        )
                    }
                }
        }
    }

    private fun calculateDiscount(voucher: Voucher, subtotal: Double): Double {
        val rawDiscount = if (voucher.type == VoucherType.PERCENT) {
            subtotal * (voucher.discountAmount / 100.0)
        } else {
            voucher.discountAmount
        }
        return if (voucher.maxDiscountAmount != null) {
            rawDiscount.coerceAtMost(voucher.maxDiscountAmount)
        } else {
            rawDiscount
        }
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
            is CartEvent.ApplyVoucher -> {
                val discount = calculateDiscount(event.voucher, _state.value.subTotal)
                _state.update { it.copy(selectedVoucher = event.voucher, discount = discount) }
            }
            is CartEvent.PromoCodeChanged -> _state.update { it.copy(promoCode = event.code, promoError = null) }
            is CartEvent.ApplyPromoCode -> handleApplyPromoCode()
            is CartEvent.ProceedToCheckout -> handleProceedToCheckout()
            is CartEvent.SelectRestaurant -> {
                if (_state.value.selectedRestaurantName != event.restaurantName) {
                    _state.update {
                        it.copy(
                            selectedRestaurantName = event.restaurantName,
                            selectedVoucher = null,
                            promoCode = "",
                            promoError = null,
                            discount = 0.0
                        )
                    }
                    loadSuitableVouchers()
                }
            }
        }
    }

    private fun handleApplyPromoCode() {
        val code = _state.value.promoCode
        if (code.isBlank()) {
            _state.update { it.copy(promoError = "Invalid code") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, promoError = null) }
            val restaurantId = _state.value.selectedItems.firstOrNull()?.restaurantId?.toIntOrNull()

            voucherRepository.getVoucherByCode(code, restaurantId)
                .onSuccess { voucherDto ->
                    val voucher = voucherDto.toDomain()
                    if (voucher.minOrderAmount > _state.value.subTotal) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                promoError = "Min. Order: $${voucher.minOrderAmount}"
                            )
                        }
                    } else {
                        val discount = calculateDiscount(voucher, _state.value.subTotal)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                selectedVoucher = voucher,
                                discount = discount,
                                promoCode = "",
                                promoError = null
                            )
                        }
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(isLoading = false, promoError = "Invalid or expired code")
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
                            discount = currentState.discount,
                            voucherId = currentState.selectedVoucher?.id
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
