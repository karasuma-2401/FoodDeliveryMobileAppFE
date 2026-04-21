package com.example.fooddelivery.ui.screens.vendor.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val isLoading: Boolean = false,
    val runningOrders: Int = 0,
    val orderRequest: Int = 0,
    val revenue: Double = 0.0,
    val rating: Double = 0.0,
    val totalReviews: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Fake API (sau này thay bằng repository)
            delay(1000)

            _state.value = DashboardState(
                runningOrders = 20,
                orderRequest = 5,
                revenue = 2241.0,
                rating = 4.9,
                totalReviews = 20
            )
        }
    }
}