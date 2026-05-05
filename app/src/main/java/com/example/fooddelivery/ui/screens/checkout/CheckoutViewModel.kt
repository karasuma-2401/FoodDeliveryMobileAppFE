package com.example.fooddelivery.ui.screens.checkout

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.R
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
    val paymentMethod: PaymentMethod = PaymentMethod.MoMo,
    val orderNote: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 10.0,
    val isLoading: Boolean = false,
    val isPolling: Boolean = false
) {
    val deliveryFee: Double get() = selectedDeliveryOption.fee
    val total: Double get() = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)
}

enum class DeliveryOption(val title: String, val time: String, val fee: Double) {
    STANDARD("Standard", "20-30 min", 5.0),
    EXPRESS("Express", "10-15 min", 10.0)
}

sealed class PaymentMethod(@StringRes val titleRes: Int) {
    data object Cash : PaymentMethod(R.string.cash_title)
    data object MoMo : PaymentMethod(R.string.momo_title)
}

sealed interface CheckoutEvent {
    data class NoteChanged(val note: String) : CheckoutEvent
    data class DeliveryOptionSelected(val option: DeliveryOption) : CheckoutEvent
    data object ChangeAddress : CheckoutEvent
    data class PaymentMethodSelected(val method: PaymentMethod) : CheckoutEvent
    data object ChangePaymentMethod : CheckoutEvent
    data object PlaceOrder : CheckoutEvent
    data object ReturnFromMoMo : CheckoutEvent
}

sealed interface CheckoutUiEffect {
    data object NavigateToAddAddress : CheckoutUiEffect
    data class OpenMoMoApp(val total: Double) : CheckoutUiEffect
    data object NavigateToPaymentSuccessful : CheckoutUiEffect
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
            is CheckoutEvent.PaymentMethodSelected -> {
                _state.update { it.copy(paymentMethod = event.method) }
            }
            is CheckoutEvent.PlaceOrder -> {
                handlePlaceOrder()
            }
            is CheckoutEvent.ReturnFromMoMo -> {
                startPolling()
            }
            else -> {}
        }
    }

    private fun handlePlaceOrder() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isPolling) return

        viewModelScope.launch {
            if (currentState.paymentMethod is PaymentMethod.Cash) {
                _state.update { it.copy(isLoading = true) }
                // Simulate API call
                delay(2000)
                cartRepository.clearCart()
                _state.update { it.copy(isLoading = false) }
                _uiEffect.emit(CheckoutUiEffect.NavigateToPaymentSuccessful)
            } else {
                // MoMo
                _state.update { it.copy(isLoading = true) }
                _uiEffect.emit(CheckoutUiEffect.OpenMoMoApp(currentState.total))
            }
        }
    }

    private fun startPolling() {
        if (_state.value.isPolling) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = false, isPolling = true) }
            // Simulate Polling Backend
            repeat(3) {
                delay(2000)
            }
            cartRepository.clearCart()
            _state.update { it.copy(isPolling = false) }
            _uiEffect.emit(CheckoutUiEffect.NavigateToPaymentSuccessful)
        }
    }
}
