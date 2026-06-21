package com.example.fooddelivery.ui.screens.admin.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminSettingUiState(
    val adminEmail: String = "",
    val totalEarnings: String = "$0.00",
    val isLoading: Boolean = false,
    val isLoggedOutSuccessfully: Boolean = false
)

class AdminSettingViewModel : ViewModel() {

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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoggedOutSuccessfully = true
                )
            }
        }
    }

    fun clearLogoutFlag() {
        _uiState.update { it.copy(isLoggedOutSuccessfully = false) }
    }
}