package com.example.fooddelivery.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.repository.AddressRepository
import com.example.fooddelivery.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutState(
    val address: Address? = null,
    val selectedDeliveryOption: DeliveryOption = DeliveryOption.STANDARD,
    val paymentMethod: PaymentMethod = PaymentMethod.Card("Mastercard", "436", "12/26"),
    val orderNote: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 10.0,
    val isLoading: Boolean = false,
    val isOrderPlaced: Boolean = false
) {
    val deliveryFee: Double get() = selectedDeliveryOption.fee
    val total: Double get() = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)
}

enum class DeliveryOption(val title: String, val time: String, val fee: Double) {
    STANDARD("Standard", "20-30 min", 5.0),
    EXPRESS("Express", "10-15 min", 10.0)
}

sealed class PaymentMethod(val title: String) {
    data object Cash : PaymentMethod("Cash on Delivery")
    data class Card(val cardName: String, val lastFour: String, val expiry: String) : PaymentMethod("$cardName **** $lastFour")
}

sealed interface CheckoutEvent {
    data class NoteChanged(val note: String) : CheckoutEvent
    data class DeliveryOptionSelected(val option: DeliveryOption) : CheckoutEvent
    data object ChangeAddress : CheckoutEvent
    data object ChangePaymentMethod : CheckoutEvent
    data object PlaceOrder : CheckoutEvent
}

sealed interface CheckoutUiEffect {
    data class NavigateToTrackOrder(val orderId: String) : CheckoutUiEffect
    data object NavigateToAddAddress : CheckoutUiEffect
    data object NavigateToPaymentMethod : CheckoutUiEffect
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CheckoutUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadInitialData()
        observeCart()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val result = addressRepository.getAddresses()
            result.onSuccess { addresses ->
                val defaultAddress = addresses.find { it.isDefault } ?: addresses.firstOrNull()
                _state.update { it.copy(address = defaultAddress) }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.getCartItems().collectLatest { items ->
                val subtotal = items.sumOf { it.totalPrice }
                _state.update { it.copy(subtotal = subtotal) }
            }
        }
    }

    fun onEvent(event: CheckoutEvent) {
        when (event) {
            is CheckoutEvent.NoteChanged -> {
                _state.update { it.copy(orderNote = event.note) }
            }
            is CheckoutEvent.DeliveryOptionSelected -> {
                _state.update { it.copy(selectedDeliveryOption = event.option) }
            }
            is CheckoutEvent.ChangeAddress -> {
                viewModelScope.launch {
                    _uiEffect.emit(CheckoutUiEffect.NavigateToAddAddress)
                }
            }
            is CheckoutEvent.ChangePaymentMethod -> {
                viewModelScope.launch {
                    _uiEffect.emit(CheckoutUiEffect.NavigateToPaymentMethod)
                }
            }
            is CheckoutEvent.PlaceOrder -> {
                placeOrder()
            }
        }
    }

    private fun placeOrder() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(2000)
            
            _state.update { it.copy(isLoading = false, isOrderPlaced = true) }
            cartRepository.clearCart()
            _uiEffect.emit(CheckoutUiEffect.NavigateToTrackOrder("ORD-77889"))
        }
    }
}
