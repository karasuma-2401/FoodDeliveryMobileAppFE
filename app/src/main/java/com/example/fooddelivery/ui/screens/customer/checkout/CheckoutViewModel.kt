package com.example.fooddelivery.ui.screens.customer.checkout

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.R
import com.example.fooddelivery.data.remote.dto.OrderItemRequest
import com.example.fooddelivery.data.remote.dto.OrderRequest
import com.example.fooddelivery.data.remote.dto.toDomain
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import com.example.fooddelivery.domain.repository.AddressRepository
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.OrderRepository
import com.example.fooddelivery.domain.repository.PaymentRepository
import com.example.fooddelivery.domain.repository.VoucherRepository
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
    val paymentMethod: PaymentMethod = PaymentMethod.MoMo,
    val orderNote: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val selectedVoucher: Voucher? = null,
    val availableVouchers: List<Voucher> = emptyList(),
    val promoCode: String = "",
    val promoError: String? = null,
    val isLoading: Boolean = false,
    val isPolling: Boolean = false,
    val errorMessage: String? = null
) {
    val total: Double get() = (subtotal - discount).coerceAtLeast(0.0)
}

sealed class PaymentMethod(@StringRes val titleRes: Int, val value: String) {
    data object Cash : PaymentMethod(R.string.cash_title, "CASH")
    data object MoMo : PaymentMethod(R.string.momo_title, "MOMO")
}

sealed interface CheckoutEvent {
    data class NoteChanged(val note: String) : CheckoutEvent
    data object ChangeAddress : CheckoutEvent
    data class PaymentMethodSelected(val method: PaymentMethod) : CheckoutEvent
    data object ChangePaymentMethod : CheckoutEvent
    data object PlaceOrder : CheckoutEvent
    data object ReturnFromMoMo : CheckoutEvent
    data class ApplyVoucher(val voucher: Voucher?) : CheckoutEvent
    data class PromoCodeChanged(val code: String) : CheckoutEvent
    data object ApplyPromoCode : CheckoutEvent
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
    private val voucherRepository: VoucherRepository,
    private val paymentRepository: PaymentRepository,
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
    
    private var pendingOrderId: Int? = null

    init {
        loadInitialData()
        observeCart()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val result = addressRepository.getAddresses()
            result.onSuccess { addresses ->
                val defaultAddress = addresses.firstOrNull()
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
                loadSuitableVouchers(subtotal)
            }
        }
    }

    private fun loadSuitableVouchers(cost: Double) {
        viewModelScope.launch {
            val restaurantId = checkoutArgs.restaurantId.toIntOrNull() ?: return@launch
            val result = voucherRepository.getSuitableVouchers(restaurantId, cost)
            result.onSuccess { vouchersDto ->
                val domainVouchers = vouchersDto.map { it.toDomain() }
                _state.update { currentState ->
                    val updatedSelectedVoucher = domainVouchers.find { it.id == currentState.selectedVoucher?.id }
                    val newDiscount = updatedSelectedVoucher?.let { calculateDiscount(it, currentState.subtotal) } ?: 0.0
                    
                    currentState.copy(
                        availableVouchers = domainVouchers,
                        selectedVoucher = updatedSelectedVoucher,
                        discount = if (updatedSelectedVoucher != null) newDiscount else 0.0
                    )
                }
            }
        }
    }

    private fun calculateDiscount(voucher: Voucher, subtotal: Double): Double {
        val rawDiscount = if (voucher.type == VoucherType.PERCENT) {
            (subtotal * (voucher.discountAmount / 100.0))
        } else {
            voucher.discountAmount
        }
        
        return if (voucher.maxDiscountAmount != null) {
            rawDiscount.coerceAtMost(voucher.maxDiscountAmount)
        } else {
            rawDiscount
        }
    }

    fun onEvent(event: CheckoutEvent) {
        when (event) {
            is CheckoutEvent.NoteChanged -> {
                _state.update { it.copy(orderNote = event.note) }
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
            is CheckoutEvent.ApplyVoucher -> {
                val discount = event.voucher?.let { calculateDiscount(it, _state.value.subtotal) } ?: 0.0
                _state.update { it.copy(
                    selectedVoucher = event.voucher,
                    discount = discount
                ) }
            }
            is CheckoutEvent.PromoCodeChanged -> {
                _state.update { it.copy(promoCode = event.code, promoError = null) }
            }
            is CheckoutEvent.ApplyPromoCode -> {
                handleApplyPromoCode()
            }
        }
    }

    private fun handleApplyPromoCode() {
        val code = _state.value.promoCode
        if (code.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, promoError = null) }
            val restaurantId = checkoutArgs.restaurantId.toIntOrNull()
            
            val result = voucherRepository.getVoucherByCode(code, restaurantId)
            
            result.onSuccess { voucherDto ->
                val voucher = voucherDto.toDomain()
                if (voucher.minOrderAmount > _state.value.subtotal) {
                    _state.update { it.copy(
                        isLoading = false, 
                        promoError = "Min. Order: $${voucher.minOrderAmount}"
                    ) }
                } else {
                    val discount = calculateDiscount(voucher, _state.value.subtotal)
                    _state.update { it.copy(
                        isLoading = false,
                        selectedVoucher = voucher,
                        discount = discount,
                        promoCode = ""
                    ) }
                }
            }.onFailure {
                _state.update { it.copy(
                    isLoading = false, 
                    promoError = "Invalid or expired code"
                ) }
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
                restaurantId = cartItems.first().restaurantId.toIntOrNull() ?: 0,
                voucherId = currentState.selectedVoucher?.id,
                savedAddressId = currentState.address.id,
                orderFoods = cartItems.map { item ->
                    OrderItemRequest(
                        foodId = item.food.id.toIntOrNull() ?: 0,
                        quantity = item.quantity,
                        fullText = item.note,
                        foodSizeId = item.foodSizeId?.toIntOrNull()
                    )
                },
                paymentMethod = currentState.paymentMethod.value,
                note = currentState.orderNote,
                clearCartAfterOrder = true,
                totalAmount = null
            )

            val result = orderRepository.createOrder(orderRequest)

            result.onSuccess { response ->
                pendingOrderId = response.order.id
                if (currentState.paymentMethod is PaymentMethod.Cash) {
                    _state.update { it.copy(isLoading = false) }
                    _uiEffect.emit(CheckoutUiEffect.NavigateToPaymentSuccessful)
                } else {
                    _state.update { it.copy(isLoading = false) }
                    response.momoPayment?.deeplink?.let {
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
            val maxAttempts = 15
            
            while (attempts < maxAttempts && !isPaid) {
                delay(6000)
                val paymentResult = paymentRepository.getPaymentDetail(orderId)

                paymentResult.onSuccess { payment ->
                    if (payment.paymentStatus == "DONE") {
                        isPaid = true
                    }
                }
                attempts++
            }

            _state.update { it.copy(isPolling = false) }

            if (isPaid) {
                pendingOrderId = null
                _uiEffect.emit(CheckoutUiEffect.NavigateToPaymentSuccessful)
            } else {
                _uiEffect.emit(CheckoutUiEffect.ShowError("Payment confirmation is taking longer than expected. Please check your order tracking."))
            }
        }
    }
}
