package com.example.fooddelivery.ui.screens.restaurant.revenue

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.RevenueDetailItem
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestaurantRevenueState(
    val isLoading: Boolean = false,
    val transactions: List<RevenueDetailItem> = emptyList(),
    val totalOrders: Int = 0,
    val totalGrossRevenue: Double = 0.0,
    val totalCommission: Double = 0.0,
    val totalNetRevenue: Double = 0.0,
    val errorMessage: String? = null
)

sealed interface RestaurantRevenueUiEffect {
    data class ShowSnackBar(val message: String) : RestaurantRevenueUiEffect
}

@HiltViewModel
class RestaurantRevenueViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val restaurantId: Int = savedStateHandle.get<Int>("restaurantId") ?: 5

    private val _state = MutableStateFlow(RestaurantRevenueState())
    val state: StateFlow<RestaurantRevenueState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<RestaurantRevenueUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadRevenueDetails()
    }

    fun loadRevenueDetails() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            restaurantRepository.getRestaurantRevenue(restaurantId)
                .onSuccess { revenueDomain ->
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            totalNetRevenue = revenueDomain.netRevenue,
                            totalGrossRevenue = revenueDomain.grossRevenue,
                            totalCommission = revenueDomain.platformFee,
                            totalOrders = revenueDomain.totalOrders,
                            transactions = revenueDomain.orderHistory,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    fun refreshRevenue() {
        loadRevenueDetails()
    }
}