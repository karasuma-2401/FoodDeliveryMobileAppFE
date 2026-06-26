package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.AdminDashboardResponse
import com.example.fooddelivery.data.remote.dto.AdminPaymentDto
import com.example.fooddelivery.data.remote.dto.AdminUserListResponse
import com.example.fooddelivery.data.remote.dto.AdminRevenueDataDto
import com.example.fooddelivery.data.remote.dto.RestaurantItemDto

interface AdminRepository {
    suspend fun getDashboard(): Result<AdminDashboardResponse>
    suspend fun getAdminPayments(): Result<List<AdminPaymentDto>>

    suspend fun getAdminUsers(): Result<AdminUserListResponse>
    suspend fun toggleUserActive(userId: Int): Result<Unit>
    suspend fun getAdminRevenue(): Result<AdminRevenueDataDto>
    suspend fun getMyRestaurants(): Result<List<RestaurantItemDto>>
    suspend fun updateRestaurantApproval(restaurantId: Int, status: String): Result<Unit>
}
