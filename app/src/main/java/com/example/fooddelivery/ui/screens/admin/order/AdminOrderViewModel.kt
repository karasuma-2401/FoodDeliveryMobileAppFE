package com.example.fooddelivery.ui.screens.admin.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.remote.dto.*
import com.example.fooddelivery.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class AdminOrderUiState(
    val isLoading: Boolean = false,
    val masterOrdersList: List<PaymentUiModel> = emptyList(),
    val filteredOrdersList: List<PaymentUiModel> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "ALL",
    val selectedRestaurantFilter: String = "ALL",
    val availableRestaurants: List<String> = emptyList(),
    val error: String? = null
)

sealed interface AdminOrderEvent {
    object Refresh : AdminOrderEvent
    data class OnSearchQueryChanged(val query: String) : AdminOrderEvent
    data class OnStatusFilterChanged(val status: String) : AdminOrderEvent
    data class OnRestaurantFilterChanged(val restaurant: String) : AdminOrderEvent
}

data class PaymentUiModel(
    val paymentId: Int,
    val orderId: Int,
    val customerName: String,
    val restaurantName: String,
    val amountText: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val orderStatus: String,
    val dateText: String
)

@HiltViewModel
class AdminOrderViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminOrderUiState())
    val uiState: StateFlow<AdminOrderUiState> = _uiState.asStateFlow()

    init {
        fetchAdminOrders()
    }

    fun onEvent(event: AdminOrderEvent) {
        when (event) {
            AdminOrderEvent.Refresh -> fetchAdminOrders()
            is AdminOrderEvent.OnSearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                applyFilters()
            }
            is AdminOrderEvent.OnStatusFilterChanged -> {
                _uiState.update { it.copy(selectedStatusFilter = event.status) }
                applyFilters()
            }
            is AdminOrderEvent.OnRestaurantFilterChanged -> {
                _uiState.update { it.copy(selectedRestaurantFilter = event.restaurant) }
                applyFilters()
            }
        }
    }

    fun fetchAdminOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getAdminPayments()
                .onSuccess { rawList ->
                    val uiModels = rawList.map { it.toUiModel() }
                    val restaurants = uiModels.map { it.restaurantName }.distinct().sorted()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            masterOrdersList = uiModels,
                            availableRestaurants = restaurants
                        )
                    }
                    applyFilters()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load data"
                        )
                    }
                }
        }
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        var result = currentState.masterOrdersList

        if (currentState.searchQuery.isNotBlank()) {
            result = result.filter {
                it.orderId.toString().contains(currentState.searchQuery, ignoreCase = true) ||
                        it.customerName.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        when (currentState.selectedStatusFilter.uppercase()) {
            "DONE" -> {
                result = result.filter { it.paymentStatus.uppercase() == "DONE" }
            }
            "OTHERS" -> {
                result = result.filter { it.paymentStatus.uppercase() != "DONE" }
            }
        }
        if (currentState.selectedRestaurantFilter != "ALL") {
            result = result.filter { it.restaurantName == currentState.selectedRestaurantFilter }
        }

        _uiState.update { it.copy(filteredOrdersList = result) }
    }

    private fun AdminPaymentDto.toUiModel(): PaymentUiModel {
        val rawDate = this.createdAt ?: ""
        val parsedDate = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outputFormat = SimpleDateFormat("HH:mm - MM/dd/yyyy", Locale.getDefault())
            val date = inputFormat.parse(rawDate)
            if (date != null) outputFormat.format(date) else rawDate
        } catch (e: Exception) {
            rawDate
        }

        return PaymentUiModel(
            paymentId = this.id,
            orderId = this.orderId,
            customerName = this.order?.user?.name ?: "Unknown Customer",
            restaurantName = this.order?.restaurant?.name?.trim() ?: "Unknown Restaurant",
            amountText = "$${String.format(Locale.US, "%.2f", this.amount)}",
            paymentMethod = this.method ?: "UNKNOWN",
            paymentStatus = this.paymentStatus ?: "UNKNOWN",
            orderStatus = this.order?.status ?: "UNKNOWN",
            dateText = parsedDate
        )
    }
}