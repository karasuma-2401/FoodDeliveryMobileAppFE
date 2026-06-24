package com.example.fooddelivery.ui.screens.customer.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Order
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderState(
    val selectedTab: Int = 0,
    val ongoingOrders: List<Order> = emptyList(),
    val historyOrders: List<Order> = emptyList(),
    val isInitLoading: Boolean = false, // for skeleton loading
    val isLoading : Boolean = false,
)

sealed interface OrderEvent {
    data class SelectTab(val index: Int) : OrderEvent
    data class CancelOrder(val orderId: String) : OrderEvent
    data class ReOrder(val orderId: String) : OrderEvent
}

sealed interface OrderUiEffect {
    data object NavigateToCart : OrderUiEffect
    data class ShowError(val message: String) : OrderUiEffect
    data class ShowSuccess(val message: String) : OrderUiEffect
}

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) : ViewModel() {
    private val _state = MutableStateFlow(OrderState())
    val state: StateFlow<OrderState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<OrderUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

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
        val id = orderId.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = orderRepository.cancelOrderPost(id)
            _state.update { it.copy(isLoading = false) }
            
            result.onSuccess { message ->
                _uiEffect.emit(OrderUiEffect.ShowSuccess(message))
                loadOrders()
            }.onFailure { error ->
                _uiEffect.emit(OrderUiEffect.ShowError(error.message ?: "Failed to cancel order"))
            }
        }
    }

    private fun reOrder(orderId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = orderRepository.reorder(orderId)
            
            result.onSuccess { message ->
                // Sync cart from server to local before navigating
                cartRepository.syncCart()
                _state.update { it.copy(isLoading = false) }
                _uiEffect.emit(OrderUiEffect.ShowSuccess(message))
                _uiEffect.emit(OrderUiEffect.NavigateToCart)
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false) }
                _uiEffect.emit(OrderUiEffect.ShowError(error.message ?: "Failed to reorder"))
            }
        }
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isInitLoading = true) }
            
            val ongoingResult = orderRepository.getOrders(status = "ongoing")
            val historyResult = orderRepository.getOrders(status = "history")
            
            _state.update { currentState ->
                currentState.copy(
                    ongoingOrders = ongoingResult.getOrDefault(emptyList()),
                    historyOrders = historyResult.getOrDefault(emptyList()),
                    isInitLoading = false
                )
            }
            
            ongoingResult.onFailure { error ->
                _uiEffect.emit(OrderUiEffect.ShowError("Ongoing: ${error.message}"))
            }
            historyResult.onFailure { error ->
                _uiEffect.emit(OrderUiEffect.ShowError("History: ${error.message}"))
            }
        }
    }
}
