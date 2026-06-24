package com.example.fooddelivery.ui.screens.restaurant.order

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.OrderDetail
import com.example.fooddelivery.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OrderStatus { PENDING, PREPARING, DELIVERING, DELIVERED, CANCELLED }

data class OrderItem(
    val name: String,
    val quantity: Int,
    val price: Double
)

data class OrderModel(
    val id: String,
    val orderTime: String,
    val customerName: String,
    val customerPhone: String,
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
    val updatingOrderId: String? = null,
    val error: String? = null
)

@HiltViewModel
class OrderManagementViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _state = mutableStateOf(OrderManagementState())
    val state: State<OrderManagementState> = _state

    init {
        loadOrders()
    }

    fun onTabSelected(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    fun refresh() {
        loadOrders()
    }

    fun acceptOrder(orderId: String) {
        updateOrderStatus(orderId, "PREPARING")
    }

    fun denyOrder(orderId: String) {
        updateOrderStatus(orderId, "CANCELLED")
    }

    fun completeOrder(orderId: String) {
        updateOrderStatus(orderId, "DELIVERING")
    }

    fun deliverOrder(orderId: String) {
        updateOrderStatus(orderId, "DELIVERED")
    }

    fun cancelOrder(orderId: String) {
        updateOrderStatus(orderId, "CANCELLED")
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val ongoingDeferred = async {
                orderRepository.getOrders(status = "ongoing", limit = 100, offset = 0)
            }
            val confirmedDeferred = async {
                orderRepository.getOrders(status = "confirmed", limit = 100, offset = 0)
            }
            val historyDeferred = async {
                orderRepository.getOrders(status = "history", limit = 100, offset = 0)
            }

            val ongoingResult = ongoingDeferred.await()
            val confirmedResult = confirmedDeferred.await()
            val historyResult = historyDeferred.await()
            val listError = ongoingResult.exceptionOrNull()
                ?: confirmedResult.exceptionOrNull()
                ?: historyResult.exceptionOrNull()

            if (listError != null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = listError.message ?: "Failed to load orders"
                )
                return@launch
            }

            val orderIds = (
                ongoingResult.getOrDefault(emptyList()) +
                    confirmedResult.getOrDefault(emptyList()) +
                    historyResult.getOrDefault(emptyList())
                )
                .mapNotNull { it.id.toIntOrNull() }
                .distinct()

            val orders = orderIds.map { orderId ->
                async { orderRepository.getOrderDetail(orderId) }
            }.mapNotNull { deferred ->
                deferred.await().getOrNull()
            }.map { detail ->
                detail.toOrderModel()
            }

            _state.value = _state.value.copy(
                orders = orders,
                isLoading = false,
                error = null
            )
        }
    }

    private fun updateOrderStatus(orderId: String, backendStatus: String) {
        val numericOrderId = orderId.toIntOrNull() ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(updatingOrderId = orderId, error = null)

            orderRepository.updateOrderStatus(numericOrderId, backendStatus)
                .onSuccess {
                    _state.value = _state.value.copy(updatingOrderId = null)
                    loadOrders()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        updatingOrderId = null,
                        error = error.message ?: "Failed to update order"
                    )
                }
        }
    }

    private fun OrderDetail.toOrderModel(): OrderModel {
        return OrderModel(
            id = id,
            orderTime = paymentDate ?: expectedArrival ?: "",
            customerName = customerName.ifBlank { "Customer #$id" },
            customerPhone = customerPhone.orEmpty(),
            status = backendStatus.toRestaurantOrderStatus(status),
            items = items.map { item ->
                OrderItem(
                    name = buildString {
                        append(item.name)
                        if (!item.size.isNullOrBlank()) append(" (${item.size})")
                    },
                    quantity = item.quantity,
                    price = item.price
                )
            }
        )
    }

    private fun String.toRestaurantOrderStatus(frontendStatus: String): OrderStatus {
        return when (uppercase().ifBlank { frontendStatus.uppercase() }) {
            "PENDING" -> OrderStatus.PENDING
            "CONFIRMED", "PREPARING" -> OrderStatus.PREPARING
            "DELIVERING" -> OrderStatus.DELIVERING
            "DELIVERED", "COMPLETED" -> OrderStatus.DELIVERED
            "CANCELLED", "CANCELED" -> OrderStatus.CANCELLED
            else -> OrderStatus.PENDING
        }
    }
}
