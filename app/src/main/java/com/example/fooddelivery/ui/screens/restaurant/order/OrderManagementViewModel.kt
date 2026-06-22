package com.example.fooddelivery.ui.screens.restaurant.order
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
enum class OrderStatus { REQUEST, RUNNING, COMPLETED, CANCELLED }

data class OrderItem(
    val name: String,
    val quantity: Int,
    val price: Double
)

data class OrderModel(
    val id: String,
    val orderTime: String,
    val items: List<OrderItem>,
    val status: OrderStatus
) {
    val totalPrice: Double
        get() = items.sumOf { it.price * it.quantity }
}

data class OrderManagementState(
    val selectedTab: Int = 0,
    val orders: List<OrderModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
@HiltViewModel
class OrderManagementViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(OrderManagementState())
    val state: State<OrderManagementState> = _state

    init {
        loadMockOrders()
    }

    fun onTabSelected(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    fun acceptOrder(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.RUNNING)
    }

    fun denyOrder(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }

    fun completeOrder(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.COMPLETED)
    }

    fun cancelOrder(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }

    private fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        val updatedOrders = _state.value.orders.map { order ->
            if (order.id == orderId) order.copy(status = newStatus) else order
        }
        _state.value = _state.value.copy(orders = updatedOrders)
    }

    private fun loadMockOrders() {
        _state.value = _state.value.copy(
            orders = listOf(
                OrderModel(
                    id = "#12345",
                    orderTime = "10:30 AM",
                    status = OrderStatus.REQUEST,
                    items = listOf(
                        OrderItem("Classic Burger", 2, 45000.0),
                        OrderItem("Cheese Pizza (M)", 1, 120000.0)
                    )
                ),
                OrderModel(
                    id = "#12346",
                    orderTime = "09:15 AM",
                    status = OrderStatus.RUNNING,
                    items = listOf(
                        OrderItem("Thai Biriyani", 1, 65000.0),
                        OrderItem("Iced Tea", 3, 15000.0)
                    )
                ),
                OrderModel(
                    id = "#12347",
                    orderTime = "Yesterday",
                    status = OrderStatus.COMPLETED,
                    items = listOf(
                        OrderItem("Fried Chicken", 4, 35000.0)
                    )
                )
            )
        )
    }
}