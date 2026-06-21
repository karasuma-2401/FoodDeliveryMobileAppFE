package com.example.fooddelivery.ui.screens.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    fun onEvent(event: AdminDashboardEvent) {
        when (event) {
            AdminDashboardEvent.Refresh -> loadDashboardData()
            AdminDashboardEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = adminRepository.getDashboard()
            result
                .onSuccess { dashboard ->
                    val stats = DashboardStats(
                        users = dashboard.users,
                        restaurants = dashboard.restaurants,
                        orders = dashboard.orders,
                        payments = dashboard.payments,
                        categories = dashboard.categories,
                        vouchers = dashboard.vouchers,
                        deliveredRevenue = dashboard.deliveredRevenue
                    )
                    _state.update { it.copy(stats = stats, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "Failed to load admin dashboard"
                        )
                    }
                }
        }
    }
}