package com.example.fooddelivery.ui.screens.admin.dashboard
data class DashboardStats(
    val users: Int = 0,
    val restaurants: Int = 0,
    val orders: Int = 0,
    val payments: Int = 0,
    val categories: Int = 0,
    val vouchers: Int = 0,
    val deliveredRevenue: Double = 0.0
)

data class AdminDashboardState(
    val stats: DashboardStats = DashboardStats(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AdminDashboardEvent {
    object Refresh : AdminDashboardEvent
    object ErrorDismissed : AdminDashboardEvent
}