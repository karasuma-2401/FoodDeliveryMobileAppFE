package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.AdminDashboardResponse
import com.example.fooddelivery.data.remote.dto.AdminPaymentDto
import com.example.fooddelivery.data.remote.dto.AdminUserListResponse
import com.example.fooddelivery.data.remote.dto.AdminRevenueDataDto
interface AdminRepository {
    suspend fun getDashboard(): Result<AdminDashboardResponse>
    suspend fun getAdminPayments(): Result<List<AdminPaymentDto>>

    suspend fun getAdminUsers(): Result<AdminUserListResponse>
    suspend fun toggleUserActive(userId: Int): Result<Unit>
    suspend fun getAdminRevenue(): Result<AdminRevenueDataDto>
}
