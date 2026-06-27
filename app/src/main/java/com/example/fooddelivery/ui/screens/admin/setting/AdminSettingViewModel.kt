package com.example.fooddelivery.ui.screens.admin.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.repository.AdminRepository // 🌟 Import Repository
import com.example.fooddelivery.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Locale

data class AdminSettingUiState(
    val adminEmail: String = "",
    val totalEarnings: String = "$0.00",
    val isLoading: Boolean = false,
    val isLoggedOutSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminSettingViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSettingUiState())
    val uiState: StateFlow<AdminSettingUiState> = _uiState.asStateFlow()

    init {
        loadAdminData()
    }
    fun loadAdminData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            adminRepository.getDashboard()
                .onSuccess { dashboard ->
                    val formattedEarnings = try {
                        String.format(Locale.US, "$%,.2f", dashboard.deliveredRevenue.toDouble())
                    } catch (e: Exception) {
                        "$${dashboard.deliveredRevenue}"
                    }

                    _uiState.update {
                        it.copy(
                            adminEmail = "admin@dfood.com",
                            totalEarnings = formattedEarnings,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to load admin settings data"
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