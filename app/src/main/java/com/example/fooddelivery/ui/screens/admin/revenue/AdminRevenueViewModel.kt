package com.example.fooddelivery.ui.screens.admin.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.remote.dto.AdminRevenueDataDto
import com.example.fooddelivery.data.remote.dto.RestaurantRevenueDto
import com.example.fooddelivery.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RevenueUiState(
    val isLoading: Boolean = false,
    val grossRevenue: Double = 0.0,
    val commissionRate: Double = 0.0,
    val adminRevenue: Double = 0.0,
    val filteredRestaurants: List<RestaurantRevenueDto> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class AdminRevenueViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevenueUiState())
    val uiState: StateFlow<RevenueUiState> = _uiState.asStateFlow()

    private var masterRestaurantList = emptyList<RestaurantRevenueDto>()

    init {
        fetchRevenueData()
    }

    fun fetchRevenueData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getAdminRevenue()
                .onSuccess { data ->
                    masterRestaurantList = data.restaurants
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            grossRevenue = data.grossRevenue,
                            commissionRate = data.adminCommissionRate,
                            adminRevenue = data.adminRevenue,
                            filteredRestaurants = data.restaurants
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        val filtered = if (query.isBlank()) {
            masterRestaurantList
        } else {
            masterRestaurantList.filter {
                it.restaurantName.contains(query, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(filteredRestaurants = filtered) }
    }
}