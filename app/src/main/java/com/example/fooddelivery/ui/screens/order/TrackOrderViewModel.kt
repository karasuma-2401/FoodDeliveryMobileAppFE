package com.example.fooddelivery.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.OrderAddress
import com.example.fooddelivery.domain.model.OrderDetail
import com.example.fooddelivery.domain.model.VoucherSummary
import com.example.fooddelivery.domain.repository.OrderRepository
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
    PENDING(1, "Order Received", "We have received your order"),
    CONFIRMED(2, "Confirmed", "Restaurant has confirmed your order"),
    PREPARING(3, "Preparing Food", "The chef is making your meal"),
    DELIVERING(4, "On the Way", "Your order is out for delivery"),
    COMPLETED(5, "Delivered", "Handover complete"),
}

data class OrderSummaryItem(
    val name: String,
    val quantity: Int,
    val price: Double,
    val description: String,
    val image: String,
    val note: String? = null
)

data class TrackOrderState(
    val orderId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val orderDetail: OrderDetail? = null,
    val trackingStatus: TrackingStatus = TrackingStatus.PENDING,
    val expectedArrival: String = "--:--",
    val restaurantName: String = "",
    val restaurantImage: String = "",
    val restaurantPhone: String = "",
    val items: List<OrderSummaryItem> = emptyList(),
    val restaurantId: Int = 0,
    val address: OrderAddress? = null,
    val paymentMethod: String = "",
    val paymentStatus: String = "",
    val totalPrice: Double = 0.0,
    val voucherInfo: VoucherSummary? = null,
    val note: String? = null
)

sealed interface TrackOrderEvent {
    data class Initialize(val orderId: String) : TrackOrderEvent
    data object Refresh : TrackOrderEvent
}

@HiltViewModel
class TrackOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TrackOrderState())
    val state: StateFlow<TrackOrderState> = _state.asStateFlow()

    private var pollingJob: Job? = null

    fun onEvent(event: TrackOrderEvent) {
        when (event) {
            is TrackOrderEvent.Initialize -> {
                if (_state.value.orderId != event.orderId) {
                    _state.update { it.copy(orderId = event.orderId) }
                    fetchOrderDetail(event.orderId)
                    startPolling(event.orderId)
                }
            }
            is TrackOrderEvent.Refresh -> {
                fetchOrderDetail(_state.value.orderId)
            }
        }
    }

    private fun fetchOrderDetail(orderId: String) {
        val id = orderId.toIntOrNull() ?: return
        viewModelScope.launch {
            if (_state.value.orderDetail == null) {
                _state.update { it.copy(isLoading = true, error = null) }
            }
            val result = orderRepository.getOrderDetail(id)
            _state.update { state ->
                result.fold(
                    onSuccess = { detail ->
                        // Stop polling if order reached final state (Delivered or Canceled)
                        if (detail.statusStep >= 5 || detail.backendStatus == "CANCELLED" || detail.backendStatus == "DELIVERED") {
                            stopPolling()
                        }
                        state.copy(
                            isLoading = false,
                            orderDetail = detail,
                            trackingStatus = mapToTrackingStatus(detail.statusStep),
                            expectedArrival = detail.expectedArrival ?: "--:--",
                            restaurantName = detail.restaurantName,
                            restaurantImage = detail.restaurantImage,
                            restaurantPhone = detail.restaurantPhone ?: "",
                            items = detail.items.map { 
                                OrderSummaryItem(it.name, it.quantity, it.price, it.size ?: "", it.image, it.note) 
                            },
                            restaurantId = detail.restaurantId,
                            address = detail.address,
                            paymentMethod = detail.paymentMethod,
                            paymentStatus = detail.paymentStatus,
                            totalPrice = detail.totalPrice,
                            voucherInfo = detail.voucherInfo,
                            note = detail.note
                        )
                    },
                    onFailure = { error ->
                        state.copy(isLoading = false, error = error.message)
                    }
                )
            }
        }
    }

    private fun startPolling(orderId: String) {
        stopPolling()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(10000) // Poll every 10 seconds
                fetchOrderDetail(orderId)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun mapToTrackingStatus(step: Int): TrackingStatus {
        return TrackingStatus.entries.find { it.step == step } ?: TrackingStatus.PENDING
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
