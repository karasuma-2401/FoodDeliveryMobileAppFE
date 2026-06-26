package com.example.fooddelivery.ui.screens.admin.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminSettingUiState(
    val adminEmail: String = "",
    val totalEarnings: String = "$0.00",
    val isLoading: Boolean = false,
    val isLoggedOutSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminSettingViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSettingUiState())
    val uiState: StateFlow<AdminSettingUiState> = _uiState.asStateFlow()

    init {
        loadAdminDashboardData()
    }

    private fun loadAdminDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            delay(1000)

            _uiState.update {
                it.copy(
                    adminEmail = "admin@dfood.com",
                    totalEarnings = "$124,500.80",
                    isLoading = false
                )
            }
        }
    }

    fun logout() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            logoutUseCase().onSuccess {
                _uiState.update {
                    it.copy(isLoading = false, isLoggedOutSuccessfully = true)
                }
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

    fun clearLogoutFlag() {
        _uiState.update { it.copy(isLoggedOutSuccessfully = false) }
    }
}
