package com.example.fooddelivery.ui.screens.restaurant.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val isLoading: Boolean = false,
    val runningOrders: Int = 0,
    val orderRequest: Int = 0,
    val revenue: Double = 0.0,
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val restaurantId = tokenManager.getRestaurantId.first()
            if (restaurantId == null) {
                repository.getMyRestaurants()
                    .onSuccess { list ->
                        val firstId = list.firstOrNull()?.id
                        if (firstId != null) {
                            tokenManager.saveRestaurantId(firstId)
                            loadDashboardForId(firstId)
                        } else {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = "No restaurant found for this merchant"
                                )
                            }
                        }
                    }
                    .onFailure { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load restaurant"
                            )
                        }
                    }
            } else {
                loadDashboardForId(restaurantId)
            }
        }
    }

    private suspend fun loadDashboardForId(restaurantId: Int) {
        repository.getDashboard(restaurantId)
            .onSuccess { dashboard ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        runningOrders = dashboard.runningOrders,
                        orderRequest = dashboard.orderRequest,
                        revenue = dashboard.revenue,
                        rating = dashboard.rating,
                        totalReviews = dashboard.totalReviews
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "An unknown error occurred"
                    )
                }
            }
    }
}
