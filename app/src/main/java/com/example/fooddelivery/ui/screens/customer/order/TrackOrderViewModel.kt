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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val hoursUntilAutoConfirm: Double? = null,
    val hasCustomerConfirmed: Boolean = false,
) {
    val shouldShowConfirmReceivedAction: Boolean
        get() = trackingStatus == TrackingStatus.DELIVERED && !hasCustomerConfirmed
}

sealed interface TrackOrderEvent {
    data class Initialize(val orderId: String) : TrackOrderEvent
    data object Refresh : TrackOrderEvent
    data object ConfirmReceived : TrackOrderEvent
    data object CheckPaymentStatus : TrackOrderEvent
}

sealed interface TrackOrderUiEffect {
    data object ConfirmReceivedSuccess : TrackOrderUiEffect
    data class ShowSnackBar(val message: String) : TrackOrderUiEffect
}

@HiltViewModel
class TrackOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TrackOrderState())
    val state: StateFlow<TrackOrderState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<TrackOrderUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private var pollingJob: Job? = null
    private var countdownJob: Job? = null
    private var activeFetchJob: Job? = null
    private var confirmInFlight = false
    private var expectedArrivalIso: String? = null
    private var fetchGeneration = 0

    private fun invalidateInFlightFetches() {
        fetchGeneration++
        activeFetchJob?.cancel()
        activeFetchJob = null
    }

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

    private fun fetchOrderDetail(
        orderId: String,
        isUserRefresh: Boolean = false,
        isSilentRefresh: Boolean = false,
    ) {
        val id = orderId.toIntOrNull() ?: return
        val job = viewModelScope.launch {
            val generation = fetchGeneration

            if ((_state.value.hasCustomerConfirmed || _state.value.isConfirming) && !isUserRefresh) {
                return@launch
            }

            val hasContent = _state.value.orderDetail != null
            if (!hasContent && !isSilentRefresh) {
                _state.update { it.copy(isLoading = true, error = null) }
            } else if (isUserRefresh) {
                _state.update { it.copy(isRefreshing = true, error = null) }
            }
            val result = orderRepository.getOrderDetail(id)
            if (generation != fetchGeneration || !isActive) return@launch

            _state.update { state ->
                result.fold(
                    onSuccess = { detail -> mapDetailToState(state, detail) },
                    onFailure = { error ->
                        if (isSilentRefresh &&
                            (state.hasCustomerConfirmed || state.trackingStatus == TrackingStatus.CONFIRMED)
                        ) {
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                isConfirming = false,
                            )
                        } else {
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                isConfirming = false,
                                error = error.message,
                            )
                        }
                    }
                )
            }
        }
        activeFetchJob?.cancel()
        activeFetchJob = job
        job.invokeOnCompletion {
            if (activeFetchJob === job) {
                activeFetchJob = null
            }
        }
    }

    private fun mapDetailToState(state: TrackOrderState, detail: OrderDetail): TrackOrderState {
        val resolvedStatus = resolveTrackingStatus(state, detail)
        val hasConfirmed = state.hasCustomerConfirmed || resolvedStatus == TrackingStatus.CONFIRMED
        if (hasConfirmed || resolvedStatus == TrackingStatus.CANCELLED) {
            stopPolling()
        }
        val etaState = buildEtaState(detail)
        syncCountdownTicker(
            iso = detail.expectedArrivalIso,
            isDelivering = detail.backendStatus.uppercase() == "DELIVERING" && !hasConfirmed,
        )
        val isCompleted = hasConfirmed || resolvedStatus == TrackingStatus.CONFIRMED
        val displayStatus = if (hasConfirmed) TrackingStatus.CONFIRMED else resolvedStatus
        return state.copy(
            isLoading = false,
            isRefreshing = false,
            isConfirming = false,
            hasCustomerConfirmed = hasConfirmed,
            orderDetail = detail,
            trackingStatus = displayStatus,
            expectedArrivalDisplay = if (isCompleted) null else etaState.expectedArrivalDisplay,
            deliveredAtDisplay = etaState.deliveredAtDisplay,
            countdownLabel = if (isCompleted) null else etaState.countdownLabel,
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
            autoConfirmAt = if (isCompleted) null else detail.autoConfirmAt,
            hoursUntilAutoConfirm = if (isCompleted) null else detail.hoursUntilAutoConfirm
        )
    }

    private fun resolveTrackingStatus(
        state: TrackOrderState,
        detail: OrderDetail,
    ): TrackingStatus {
        if (state.hasCustomerConfirmed) {
            return TrackingStatus.CONFIRMED
        }
        val incoming = resolveIncomingStatus(detail)
        if (incoming == TrackingStatus.CANCELLED) return TrackingStatus.CANCELLED
        if (state.trackingStatus == TrackingStatus.CANCELLED) return TrackingStatus.CANCELLED
        return if (state.trackingStatus.step >= incoming.step) state.trackingStatus else incoming
    }

    private fun confirmReceived() {
        val current = _state.value
        if (confirmInFlight || current.hasCustomerConfirmed || current.isConfirming) return
        if (current.trackingStatus != TrackingStatus.DELIVERED) return

        val orderId = current.orderId.toIntOrNull() ?: return
        val orderIdStr = current.orderId

        confirmInFlight = true
        invalidateInFlightFetches()
        stopPolling()
        _state.update { it.copy(isConfirming = true, error = null) }

        viewModelScope.launch {
            try {
                val result = orderRepository.confirmReceived(orderId)
                val alreadyConfirmed = result.exceptionOrNull()?.isAlreadyConfirmedError() == true
                if (result.isSuccess || alreadyConfirmed) {
                    syncConfirmedOrderFromServer(orderIdStr)
                    _uiEffect.emit(TrackOrderUiEffect.ConfirmReceivedSuccess)
                } else {
                    _state.update { it.copy(isConfirming = false) }
                    startPolling(orderIdStr)
                    _uiEffect.emit(
                        TrackOrderUiEffect.ShowSnackBar(
                            result.exceptionOrNull()?.message ?: "Failed to confirm receipt"
                        )
                    )
                }
            } finally {
                confirmInFlight = false
            }
        }
    }

    private suspend fun syncConfirmedOrderFromServer(orderIdStr: String) {
        val id = orderIdStr.toIntOrNull() ?: return
        val detailResult = orderRepository.getOrderDetail(id)
        _state.update { state ->
            detailResult.fold(
                onSuccess = { detail ->
                    mapDetailToState(
                        state.copy(isConfirming = false, hasCustomerConfirmed = true),
                        detail,
                    )
                },
                onFailure = {
                    markConfirmedAfterServerAck(state)
                }
            )
        }
        stopPolling()
    }

    private fun resolveIncomingStatus(detail: OrderDetail): TrackingStatus {
        val normalizedStatus = detail.status.uppercase()
        val normalizedBackend = detail.backendStatus.uppercase()
        return when {
            normalizedStatus == "CONFIRMED" || normalizedStatus == "COMPLETED" -> TrackingStatus.CONFIRMED
            normalizedBackend == "CONFIRMED" || normalizedBackend == "COMPLETED" -> TrackingStatus.CONFIRMED
            normalizedStatus == "CANCELLED" || normalizedStatus == "CANCELED" -> TrackingStatus.CANCELLED
            normalizedBackend == "CANCELLED" || normalizedBackend == "CANCELED" -> TrackingStatus.CANCELLED
            else -> mapToTrackingStatus(detail.statusStep)
        }
    }

    private fun Throwable.isAlreadyConfirmedError(): Boolean {
        val message = message?.lowercase().orEmpty()
        return message.contains("already") &&
            (message.contains("confirm") || message.contains("request"))
    }

    private fun markConfirmedAfterServerAck(state: TrackOrderState): TrackOrderState {
        countdownJob?.cancel()
        countdownJob = null
        expectedArrivalIso = null
        return state.copy(
            isConfirming = false,
            hasCustomerConfirmed = true,
            trackingStatus = TrackingStatus.CONFIRMED,
            expectedArrivalDisplay = null,
            countdownLabel = null,
            hoursUntilAutoConfirm = null,
            autoConfirmAt = null,
        )
    }

    private fun startPolling(orderId: String) {
        stopPolling()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(15_000)
                if (_state.value.isConfirming) continue
                if (_state.value.hasCustomerConfirmed ||
                    _state.value.trackingStatus == TrackingStatus.CONFIRMED ||
                    _state.value.trackingStatus == TrackingStatus.CANCELLED
                ) {
                    break
                }
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
        activeFetchJob?.cancel()
        countdownJob?.cancel()
    }

    private data class EtaState(
        val expectedArrivalDisplay: String?,
        val deliveredAtDisplay: String?,
        val countdownLabel: String?,
    )
}
