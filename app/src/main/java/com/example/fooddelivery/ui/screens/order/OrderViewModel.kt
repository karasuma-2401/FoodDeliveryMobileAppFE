package com.example.fooddelivery.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Order
import com.example.fooddelivery.domain.model.OrderStatus
import com.example.fooddelivery.domain.model.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderState(
    val selectedTab: Int = 0,
    val ongoingOrders: List<Order> = emptyList(),
    val historyOrders: List<Order> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface OrderEvent {
    data class SelectTab(val index: Int) : OrderEvent
    data class CancelOrder(val orderId: String) : OrderEvent
    data class ReOrder(val orderId: String) : OrderEvent
}

@HiltViewModel
class OrderViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(OrderState())
    val state: StateFlow<OrderState> = _state.asStateFlow()

    init {
        loadOrders()
    }

    fun onEvent(event: OrderEvent) {
        when (event) {
            is OrderEvent.SelectTab -> {
                _state.update { it.copy(selectedTab = event.index) }
            }
            is OrderEvent.CancelOrder -> {
                cancelOrder(event.orderId)
            }
            is OrderEvent.ReOrder -> {
                reOrder(event.orderId)
            }
        }
    }

    private fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // Giả lập gọi API hủy đơn
            delay(1000)
            _state.update { currentState ->
                val canceledOrder = currentState.ongoingOrders.find { it.id == orderId }
                val updatedOngoing = currentState.ongoingOrders.filter { it.id != orderId }
                
                val updatedHistory = if (canceledOrder != null) {
                    listOf(canceledOrder.copy(status = OrderStatus.CANCELED, date = "Just Now")) + currentState.historyOrders
                } else {
                    currentState.historyOrders
                }

                currentState.copy(
                    ongoingOrders = updatedOngoing,
                    historyOrders = updatedHistory,
                    isLoading = false
                )
            }
        }
    }

    private fun reOrder(orderId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // call api here
            delay(1500)
            _state.update { it.copy(isLoading = false) }
            // navigate to cart screen
        }
    }

    private fun loadOrders() {
        val mockOngoing = listOf(
            Order(
                id = "162432",
                restaurantId = "res_1",
                restaurantName = "Pizza Hut",
                restaurantImage = "https://img.freepik.com/free-photo/pizza-pizza-filled-with-tomatoes-salami-olives_140725-1200.jpg",
                price = 35.25,
                itemCount = 3,
                type = OrderType.FOOD,
                status = OrderStatus.ONGOING
            ),
            Order(
                id = "242432",
                restaurantId = "res_2",
                restaurantName = "McDonald",
                restaurantImage = "https://img.freepik.com/free-photo/delicious-burger-with-fresh-ingredients_23-2150857908.jpg",
                price = 40.15,
                itemCount = 2,
                type = OrderType.FOOD,
                status = OrderStatus.ONGOING
            ),
            Order(
                id = "240112",
                restaurantId = "res_3",
                restaurantName = "Starbucks",
                restaurantImage = "https://img.freepik.com/free-photo/cup-coffee-with-heart-drawn-it_188544-12644.jpg",
                price = 10.20,
                itemCount = 1,
                type = OrderType.DRINK,
                status = OrderStatus.ONGOING
            )
        )
        val mockHistory = listOf(
            Order(
                id = "162435",
                restaurantId = "res_1",
                restaurantName = "Pizza Hut",
                restaurantImage = "https://img.freepik.com/free-photo/pizza-pizza-filled-with-tomatoes-salami-olives_140725-1200.jpg",
                price = 35.25,
                itemCount = 3,
                type = OrderType.FOOD,
                status = OrderStatus.COMPLETED,
                date = "29 JAN, 12:30"
            ),
            Order(
                id = "242436",
                restaurantId = "res_2",
                restaurantName = "McDonald",
                restaurantImage = "https://img.freepik.com/free-photo/delicious-burger-with-fresh-ingredients_23-2150857908.jpg",
                price = 40.15,
                itemCount = 2,
                type = OrderType.FOOD,
                status = OrderStatus.COMPLETED,
                date = "30 JAN, 12:30"
            ),
            Order(
                id = "240117",
                restaurantId = "res_3",
                restaurantName = "Starbucks",
                restaurantImage = "https://img.freepik.com/free-photo/cup-coffee-with-heart-drawn-it_188544-12644.jpg",
                price = 10.20,
                itemCount = 1,
                type = OrderType.DRINK,
                status = OrderStatus.CANCELED,
                date = "30 JAN, 12:30"
            )
        )
        _state.update { it.copy(ongoingOrders = mockOngoing, historyOrders = mockHistory) }
    }
}
