package com.example.fooddelivery.ui.screens.restaurant.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val logoutUseCase: LogoutUseCase
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
