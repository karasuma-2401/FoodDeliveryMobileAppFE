package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.AdminDashboardResponse

interface AdminRepository {
    suspend fun getDashboard(): Result<AdminDashboardResponse>
}
