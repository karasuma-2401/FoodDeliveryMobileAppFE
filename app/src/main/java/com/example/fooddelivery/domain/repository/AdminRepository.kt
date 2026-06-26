package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.AdminDashboardResponse
import com.example.fooddelivery.data.remote.dto.AdminPaymentDto

interface AdminRepository {
    suspend fun getDashboard(): Result<AdminDashboardResponse>
    suspend fun getAdminPayments(): Result<List<AdminPaymentDto>>
}
