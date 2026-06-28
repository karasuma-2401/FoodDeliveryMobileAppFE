package com.example.fooddelivery.ui.screens.customer.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.OrderAddress
import com.example.fooddelivery.domain.model.OrderDetail
import com.example.fooddelivery.domain.model.VoucherSummary
import com.example.fooddelivery.domain.repository.OrderRepository
import com.example.fooddelivery.util.OrderEta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TrackingStatus(val step: Int, val title: String, val subtitle: String) {
    PENDING(0, "Order Received", "We have received your order"),
    PREPARING(1, "Preparing Food", "The chef is making your meal"),
    DELIVERING(2, "On the Way", "Your order is out for delivery"),
    DELIVERED(3, "Delivered", "Food has arrived at your location"),
    CONFIRMED(4, "Completed", "Order finished"),
    CANCELLED(-1, "Cancelled", "Order has been cancelled")
}

data class OrderSummaryItem(
    val name: String,
    val quantity: Int,
    val lineTotal: Double,
    val description: String,
    val image: String,
    val note: String? = null
)

data class TrackOrderState(
    val orderId: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isConfirming: Boolean = false,
    val error: String? = null,
    val orderDetail: OrderDetail? = null,
    val trackingStatus: TrackingStatus = TrackingStatus.PENDING,
    val expectedArrivalDisplay: String? = null,
    val deliveredAtDisplay: String? = null,
    val countdownLabel: String? = null,
    val restaurantName: String = "",
    val restaurantImage: String = "",
    val restaurantPhone: String = "",
    val items: List<OrderSummaryItem> = emptyList(),
    val restaurantId: Int = 0,
    val sellerId: Int = 0,
    val address: OrderAddress? = null,
    val paymentMethod: String = "",
    val paymentStatus: String = "",
    val totalPrice: Double = 0.0,
    val voucherInfo: VoucherSummary? = null,
    val note: String? = null,
    val autoConfirmAt: String? = null,
    val hoursUntilAutoConfirm: Double? = null
)

sealed interface TrackOrderEvent {
    data class Initialize(val orderId: String) : TrackOrderEvent
    data object Refresh : TrackOrderEvent
    data object ConfirmReceived : TrackOrderEvent
    data object CheckPaymentStatus : TrackOrderEvent
}

@HiltViewModel
class TrackOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TrackOrderState())
    val state: StateFlow<TrackOrderState> = _state.asStateFlow()

    private var pollingJob: Job? = null
    private var countdownJob: Job? = null
    private var expectedArrivalIso: String? = null

    fun onEvent(event: TrackOrderEvent) {
        when (event) {
            is TrackOrderEvent.Initialize -> {
                if (_state.value.orderId != event.orderId) {
                    _state.update { it.copy(orderId = event.orderId) }
                    fetchOrderDetail(event.orderId, isUserRefresh = false)
                    startPolling(event.orderId)
                }
            }
            is TrackOrderEvent.Refresh -> {
                fetchOrderDetail(_state.value.orderId, isUserRefresh = true)
            }
            is TrackOrderEvent.ConfirmReceived -> {
                confirmReceived()
            }
            is TrackOrderEvent.CheckPaymentStatus -> {
                fetchOrderDetail(_state.value.orderId, isUserRefresh = false)
            }
        }
    }

    private fun fetchOrderDetail(orderId: String, isUserRefresh: Boolean) {
        val id = orderId.toIntOrNull() ?: return
        viewModelScope.launch {
            val hasContent = _state.value.orderDetail != null
            if (!hasContent) {
                _state.update { it.copy(isLoading = true, error = null) }
            } else if (isUserRefresh) {
                _state.update { it.copy(isRefreshing = true, error = null) }
            }
            val result = orderRepository.getOrderDetail(id)
            _state.update { state ->
                result.fold(
                    onSuccess = { detail ->
                        if (detail.statusStep == 4 || detail.statusStep == -1 ||
                            detail.backendStatus == "CONFIRMED" || detail.backendStatus == "CANCELLED"
                        ) {
                            stopPolling()
                        }
                        val etaState = buildEtaState(detail)
                        syncCountdownTicker(
                            iso = detail.expectedArrivalIso,
                            isDelivering = detail.backendStatus.uppercase() == "DELIVERING",
                        )
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isConfirming = false,
                            orderDetail = detail,
                            trackingStatus = mapToTrackingStatus(detail.statusStep),
                            expectedArrivalDisplay = etaState.expectedArrivalDisplay,
                            deliveredAtDisplay = etaState.deliveredAtDisplay,
                            countdownLabel = etaState.countdownLabel,
                            restaurantName = detail.restaurantName,
                            restaurantImage = detail.restaurantImage,
                            restaurantPhone = detail.restaurantPhone ?: "",
                            items = detail.items.map {
                                OrderSummaryItem(
                                    name = it.name,
                                    quantity = it.quantity,
                                    lineTotal = it.lineTotal,
                                    description = it.size ?: "",
                                    image = it.image,
                                    note = it.note
                                )
                            },
                            restaurantId = detail.restaurantId,
                            sellerId = detail.sellerId,
                            address = detail.address,
                            paymentMethod = detail.paymentMethod,
                            paymentStatus = detail.paymentStatus,
                            totalPrice = detail.totalPrice,
                            voucherInfo = detail.voucherInfo,
                            note = detail.note,
                            autoConfirmAt = detail.autoConfirmAt,
                            hoursUntilAutoConfirm = detail.hoursUntilAutoConfirm
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message,
                        )
                    }
                )
            }
        }
    }

    private fun confirmReceived() {
        val orderId = _state.value.orderId.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isConfirming = true) }
            val result = orderRepository.confirmReceived(orderId)
            result.onSuccess {
                fetchOrderDetail(orderId.toString(), isUserRefresh = false)
            }.onFailure { error ->
                _state.update { it.copy(isConfirming = false, error = error.message) }
            }
        }
    }

    private fun startPolling(orderId: String) {
        stopPolling()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(15_000)
                fetchOrderDetail(orderId, isUserRefresh = false)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun syncCountdownTicker(iso: String?, isDelivering: Boolean) {
        if (!isDelivering || iso.isNullOrBlank()) {
            expectedArrivalIso = null
            countdownJob?.cancel()
            countdownJob = null
            return
        }
        if (iso == expectedArrivalIso && countdownJob?.isActive == true) return
        expectedArrivalIso = iso
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val label = OrderEta.countdownLabel(iso)
                _state.update { it.copy(countdownLabel = label) }
                delay(30_000)
            }
        }
    }

    private fun buildEtaState(detail: OrderDetail): EtaState {
        return when (detail.backendStatus.uppercase()) {
            "DELIVERING" -> EtaState(
                expectedArrivalDisplay = detail.expectedArrival,
                deliveredAtDisplay = null,
                countdownLabel = detail.expectedArrivalIso?.let { OrderEta.countdownLabel(it) },
            )
            "DELIVERED" -> EtaState(
                expectedArrivalDisplay = null,
                deliveredAtDisplay = detail.deliveredAt,
                countdownLabel = null,
            )
            else -> EtaState(null, null, null)
        }
    }

    private fun mapToTrackingStatus(step: Int): TrackingStatus {
        return TrackingStatus.entries.find { it.step == step } ?: TrackingStatus.PENDING
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
        countdownJob?.cancel()
    }

    private data class EtaState(
        val expectedArrivalDisplay: String?,
        val deliveredAtDisplay: String?,
        val countdownLabel: String?,
    )
}
