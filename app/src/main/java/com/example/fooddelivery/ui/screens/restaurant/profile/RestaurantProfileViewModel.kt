package com.example.fooddelivery.ui.screens.restaurant.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RestaurantProfileUiState(
    val isLoading: Boolean = false,
    val balance: String = "$0.00",
    val numberOfOrders: String = "0",
    val errorMessage: String? = null
)

class RestaurantProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RestaurantProfileUiState())
    val uiState: StateFlow<RestaurantProfileUiState> = _uiState.asStateFlow()

    init {
        fetchProfileData()
    }

    fun fetchProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        balance = "$500.00",
                        numberOfOrders = "29K"
                    )
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

    fun handleWithdraw() {
        }
}
