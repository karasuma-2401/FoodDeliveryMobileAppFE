package com.example.fooddelivery.ui.screens.checkout

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.R
import com.example.fooddelivery.data.remote.dto.OrderItemRequest
import com.example.fooddelivery.data.remote.dto.OrderRequest
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.model.OrderStatus
import com.example.fooddelivery.domain.repository.AddressRepository
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.OrderRepository
import com.example.fooddelivery.ui.navigation.CheckoutRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutState(
    val restaurantName: String = "",
    val address: Address? = null,
    val selectedDeliveryOption: DeliveryOption = DeliveryOption.STANDARD,
    val paymentMethod: PaymentMethod = PaymentMethod.MoMo,
    val orderNote: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val isLoading: Boolean = false,
    val isPolling: Boolean = false,
    val errorMessage: String? = null
) {
    val deliveryFee: Double get() = selectedDeliveryOption.fee
    val total: Double get() = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)
}

enum class DeliveryOption(val title: String, val fee: Double) {
    STANDARD("Standard", 5.0),
    EXPRESS("Express", 10.0)
}

sealed class PaymentMethod(@StringRes val titleRes: Int, val value: String) {
    data object Cash : PaymentMethod(R.string.cash_title, "CASH")
    data object MoMo : PaymentMethod(R.string.momo_title, "MOMO")
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
    data class OpenMoMoApp(val deeplink: String, val total: Double) : CheckoutUiEffect
    data object NavigateToPaymentSuccessful : CheckoutUiEffect
    data class ShowError(val message: String) : CheckoutUiEffect
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val checkoutArgs = savedStateHandle.toRoute<CheckoutRoute>()

    private val _state = MutableStateFlow(
        CheckoutState(
            restaurantName = checkoutArgs.restaurantName,
            discount = checkoutArgs.discount
        )
    )
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CheckoutUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    private var pendingOrderId: String? = null

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
            cartRepository.cartItems.collectLatest { items ->
                val subtotal = items
                    .filter { it.restaurantId == checkoutArgs.restaurantId }
                    .sumOf { it.totalPrice }
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
                _state.update { it.copy(paymentMethod = if (it.paymentMethod is PaymentMethod.MoMo) PaymentMethod.Cash else PaymentMethod.MoMo) }
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
        }
    }

    private fun handlePlaceOrder() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isPolling) return
        if (currentState.address == null) {
            viewModelScope.launch {
                _uiEffect.emit(CheckoutUiEffect.ShowError("Please select a delivery address"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val cartItems = cartRepository.cartItems.first()
                .filter { it.restaurantId == checkoutArgs.restaurantId }

            if (cartItems.isEmpty()) {
                _state.update { it.copy(isLoading = false) }
                _uiEffect.emit(CheckoutUiEffect.ShowError("No items from this restaurant in cart"))
                return@launch
            }

            val orderRequest = OrderRequest(
                restaurantId = cartItems.first().restaurantId,
                items = cartItems.map { 
                    OrderItemRequest(
                        foodId = it.food.id,
                        quantity = it.quantity,
                        price = it.unitPrice,
                        size = it.size
                    )
                },
                addressId = currentState.address.id,
                deliveryOption = currentState.selectedDeliveryOption.name,
                paymentMethod = currentState.paymentMethod.value,
                note = currentState.orderNote,
                totalAmount = currentState.total
            )

            val result = orderRepository.createOrder(orderRequest)

            result.onSuccess { response ->
                if (currentState.paymentMethod is PaymentMethod.Cash) {
                    // Clear only items from this restaurant
                    cartItems.forEach { item ->
                        cartRepository.removeItem(
                            foodId = item.food.id,
                            size = item.size,
                            restaurantId = item.restaurantId
                        )
                    }
                    _state.update { it.copy(isLoading = false) }
                    _uiEffect.emit(CheckoutUiEffect.NavigateToPaymentSuccessful)
                } else {
                    pendingOrderId = response.orderId
                    _state.update { it.copy(isLoading = false) }
                    response.deeplink?.let {
                        _uiEffect.emit(CheckoutUiEffect.OpenMoMoApp(it, currentState.total))
                    } ?: run {
                        _uiEffect.emit(CheckoutUiEffect.ShowError("Failed to get payment link"))
                    }
                }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false) }
                _uiEffect.emit(CheckoutUiEffect.ShowError(error.message ?: "Failed to place order"))
            }
        }
    }

    private fun startPolling() {
        val orderId = pendingOrderId ?: return
        if (_state.value.isPolling) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = false, isPolling = true) }
            var isPaid = false
            var attempts = 0
            val maxAttempts = 10
            while (attempts < maxAttempts && !isPaid) {
                delay(3000)
                val statusResult = orderRepository.checkOrderStatus(orderId)

                statusResult.onSuccess { status ->
                    if (status == OrderStatus.COMPLETED) {
                        isPaid = true
                    }
                }
                attempts++
            }

            _state.update { it.copy(isPolling = false) }

            if (isPaid) {
                val cartItems = cartRepository.cartItems.first()
                    .filter { it.restaurantId == checkoutArgs.restaurantId }
                cartItems.forEach { item ->
                    cartRepository.removeItem(
                        foodId = item.food.id,
                        size = item.size,
                        restaurantId = item.restaurantId
                    )
                }

                pendingOrderId = null
                _uiEffect.emit(CheckoutUiEffect.NavigateToPaymentSuccessful)
            } else {
                _uiEffect.emit(CheckoutUiEffect.ShowError("Payment failed or timed out"))
            }
        }
    }
}
