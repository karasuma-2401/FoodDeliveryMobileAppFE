package com.example.fooddelivery.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TrackingStatus(val step: Int, val title: String, val subtitle: String) {
    RECEIVED(0, "Order Received", "We have received your order"),
    PREPARING(1, "Preparing Food", "The chef is making your meal"),
    ON_THE_WAY(2, "On the Way", "Your order is out for delivery"),
    DELIVERED(3, "Delivered", "Handover complete"),
}
data class OrderSummaryItem(
    val name: String,
    val quantity: Int,
    val description: String,
    val image: String
)
data class TrackOrderState(
    val orderId: String = "",
    val restaurantId: Int = 0,
    val expectedArrival: String = "12:45 PM",
    val status: TrackingStatus = TrackingStatus.RECEIVED,
    val restaurantName: String = "Rose Garden Restaurant",
    val restaurantImage: String = "https://example.com/logo.jpg",
    val restaurantPhone: String = "0987654321",
    val items: List<OrderSummaryItem> = emptyList()
)
sealed interface TrackOrderEvent {
    data class Initialize(val orderId: String) : TrackOrderEvent
}

@HiltViewModel
class TrackOrderViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(TrackOrderState())
    val state: StateFlow<TrackOrderState> = _state.asStateFlow()
    fun onEvent(event: TrackOrderEvent) {
        when (event) {
            is TrackOrderEvent.Initialize -> {
                setupInitialData(event.orderId)
            }
        }
    }
    private fun setupInitialData(id: String) {
        _state.update { it.copy(
            orderId = id,
            restaurantId = 6,
            status = TrackingStatus.RECEIVED,
            items = listOf(
                OrderSummaryItem("Burger Bistro", 1, "Extra cheese, No onions", "https://example.com/burger.jpg"),
                OrderSummaryItem("Garden Pizza", 1, "Medium size, thin crust", "https://example.com/pizza.jpg")
            )
        ) }
        startPollingStatus()
    }
    private fun startPollingStatus() {
        viewModelScope.launch {
            delay(3000)
            _state.update { it.copy(status = TrackingStatus.PREPARING) }

            delay(4000)
            _state.update { it.copy(status = TrackingStatus.ON_THE_WAY) }

            delay(5000)
            _state.update { it.copy(status = TrackingStatus.DELIVERED) }
        }
    }
}
