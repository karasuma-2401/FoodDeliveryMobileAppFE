package com.example.fooddelivery.ui.screens.restaurant.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.usecase.LogoutUseCase
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestaurantProfileUiState(
    val isLoading: Boolean = false,
    val restaurantId: Int = 0,
    val balance: String = "$0.00",
    val numberOfOrders: String = "0",
    val errorMessage: String? = null,
    val isLogoutSuccess: Boolean = false
)

@HiltViewModel
class RestaurantProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val repository: RestaurantRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestaurantProfileUiState())
    val uiState: StateFlow<RestaurantProfileUiState> = _uiState.asStateFlow()

    init {
        fetchProfileData()
    }

    fun fetchProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val restaurantId = tokenManager.getRestaurantId.first()
                if (restaurantId == null) {
                    repository.getMyRestaurants()
                        .onSuccess { list ->
                            val firstRestaurant = list.firstOrNull()
                            if (firstRestaurant != null) {
                                tokenManager.saveRestaurantId(firstRestaurant.id)
                                loadRevenueForId(firstRestaurant.id)
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "No restaurant found for this merchant"
                                    )
                                }
                            }
                        }
                        .onFailure { error ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = error.message ?: "Failed to load restaurant"
                                )
                            }
                        }
                } else {
                    loadRevenueForId(restaurantId)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Error"
                    )
                }
            }
        }
    }

    private suspend fun loadRevenueForId(restaurantId: Int) {
        repository.generateDashboard(restaurantId)
            .onSuccess { dashboard ->
                val formatted = CurrencyFormatter.format(dashboard.revenue)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        balance = formatted,
                        restaurantId = restaurantId,
                        numberOfOrders = dashboard.totalOrders.toString()
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load revenue"
                    )
                }
            }
    }

    fun logout() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            logoutUseCase().onSuccess {
                _uiState.update { it.copy(isLoading = false, isLogoutSuccess = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to log out"
                    )
                }
            }
        }
    }

    fun clearLogoutSuccess() {
        _uiState.update { it.copy(isLogoutSuccess = false) }
    }

    fun handleWithdraw() {
    }
}