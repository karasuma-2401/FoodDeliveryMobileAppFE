package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.AdminDashboardResponse
import com.example.fooddelivery.data.remote.dto.AdminPaymentDto
import com.example.fooddelivery.data.remote.dto.AdminUserListResponse

interface AdminRepository {
    suspend fun getDashboard(): Result<AdminDashboardResponse>
    suspend fun getAdminPayments(): Result<List<AdminPaymentDto>>

    suspend fun getAdminUsers(): Result<AdminUserListResponse>
    suspend fun toggleUserActive(userId: Int): Result<Unit>
}
