package com.example.fooddelivery.ui.screens.restaurant.order

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.OrderDetail
import com.example.fooddelivery.domain.repository.OrderRepository
import com.example.fooddelivery.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OrderStatus { PENDING, PREPARING, DELIVERING, DELIVERED, CONFIRMED, CANCELLED }

data class OrderItem(
    val name: String,
    val quantity: Int,
    val lineTotal: Double
)

data class OrderModel(
    val id: String,
    val orderTime: String,
    val customerName: String,
    val customerPhone: String,
    val conversationId: Int? = null,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val isError: Boolean = false
) {
    val totalPrice: Double
        get() = items.sumOf { it.lineTotal }
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
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _state = mutableStateOf(OrderManagementState())
    val state: State<OrderManagementState> = _state

    init {
        loadOrders()
    }

    fun onTabSelected(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    fun loadOrders() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val ongoingDeferred = async {
                orderRepository.getOrders(status = "ongoing", limit = 1000, offset = 0)
            }
            val historyDeferred = async {
                orderRepository.getOrders(status = "history", limit = 1000, offset = 0)
            }

            val ongoingResult = ongoingDeferred.await()
            val historyResult = historyDeferred.await()

            val listError = ongoingResult.exceptionOrNull()
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
                            historyResult.getOrDefault(emptyList())
                    )
                .mapNotNull { it.id.toIntOrNull() }
                .distinct()

            val ordersDeferred = orderIds.map { orderId ->
                async { orderId to orderRepository.getOrderDetail(orderId) }
            }

            val orders = ordersDeferred.awaitAll().map { (orderId, result) ->
                result.fold(
                    onSuccess = { detail -> toOrderModel(detail) },
                    onFailure = {
                        OrderModel(
                            id = orderId.toString(),
                            orderTime = "",
                            customerName = "Error loading order #$orderId",
                            customerPhone = "",
                            items = emptyList(),
                            status = OrderStatus.PENDING,
                            isError = true
                        )
                    }
                )
            }

            _state.value = _state.value.copy(
                orders = orders,
                isLoading = false,
                error = null
            )
        }
    }

    fun acceptOrder(orderId: String) {
        updateOrderStatus(orderId, newStatus = "PREPARING")
    }

    fun denyOrder(orderId: String) {
        updateOrderStatus(orderId, newStatus = "CANCELLED")
    }

    fun completeOrder(orderId: String) {
        updateOrderStatus(orderId, newStatus = "DELIVERING")
    }

    fun deliverOrder(orderId: String) {
        val numericOrderId = orderId.toIntOrNull() ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(updatingOrderId = orderId, error = null)

            val paymentResult = paymentRepository.getPaymentDetail(numericOrderId)

            paymentResult.onSuccess { payment ->
                val confirmResult = paymentRepository.confirmPayment(payment.id)

                confirmResult.onSuccess {
                    updateOrderStatusInternal(orderId, numericOrderId, "DELIVERED")
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        updatingOrderId = null,
                        error = "Payment confirmation failed: ${error.message}"
                    )
                }
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    updatingOrderId = null,
                    error = "Could not load payment details: ${error.message}"
                )
            }
        }
    }

    fun cancelOrder(orderId: String) {
        updateOrderStatus(orderId, newStatus = "CANCELLED")
    }

    private fun updateOrderStatus(orderId: String, newStatus: String) {
        val numericOrderId = orderId.toIntOrNull() ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(updatingOrderId = orderId, error = null)
            updateOrderStatusInternal(orderId, numericOrderId, newStatus)
        }
    }

    private suspend fun updateOrderStatusInternal(orderId: String, numericOrderId: Int, newStatus: String) {
        orderRepository.updateOrderStatus(numericOrderId, newStatus)
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

    private fun toOrderModel(detail: OrderDetail): OrderModel {
        return OrderModel(
            id = detail.id,
            orderTime = detail.paymentDate ?: detail.expectedArrival ?: "",
            customerName = detail.customerName.ifBlank { "Customer #${detail.id}" },
            customerPhone = detail.customerPhone.orEmpty(),
            conversationId = detail.conversationId,
            status = mapBackendStatus(detail.backendStatus, detail.status),
            items = detail.items.map { item ->
                OrderItem(
                    name = buildString {
                        append(item.name)
                        if (!item.size.isNullOrBlank()) append(" (${item.size})")
                    },
                    quantity = item.quantity,
                    lineTotal = item.lineTotal
                )
            }
        )
    }

    private fun mapBackendStatus(backendSt: String, frontendSt: String): OrderStatus {
        return when (backendSt.uppercase().ifBlank { frontendSt.uppercase() }) {
            "PENDING"        -> OrderStatus.PENDING
            "PREPARING"      -> OrderStatus.PREPARING
            "DELIVERING"     -> OrderStatus.DELIVERING
            "DELIVERED"      -> OrderStatus.DELIVERED
            "CONFIRMED"      -> OrderStatus.CONFIRMED
            "CANCELLED",
            "CANCELED"       -> OrderStatus.CANCELLED
            else             -> OrderStatus.PENDING
        }
    }
}