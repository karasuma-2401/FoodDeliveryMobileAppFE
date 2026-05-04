package com.example.fooddelivery.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.CartItem
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
    val discount: Double = 10.0,
    val isLoading: Boolean = false
) {
    val subTotal: Double get() = items.sumOf { it.totalPrice }
    val total: Double get() = (subTotal + deliveryFee - discount).coerceAtLeast(0.0)
    val isCartEmpty: Boolean get() = items.isEmpty()
}
sealed interface CartEvent {
    data class UpdateQuantity(val foodId: String, val size: String, val delta: Int) : CartEvent
    data class RemoveItem(val foodId: String, val size: String): CartEvent
    data object ClearCart: CartEvent
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
    }
    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.getCartItems().collectLatest { items ->
                _state.update { it.copy(items = items) }
            }
        }
    }
    fun onEvent(event: CartEvent) {
        when (event) {
            is CartEvent.UpdateQuantity -> {
                cartRepository.updateQuantity(event.foodId, event.size, event.delta)
            }
            is CartEvent.RemoveItem -> {
                cartRepository.removeItem(event.foodId, event.size)
            }
            is CartEvent.ClearCart -> {
                cartRepository.clearCart()
            }
            is CartEvent.ProceedToCheckout -> {
                // navigate to checkout screen
            }
        }
    }
}