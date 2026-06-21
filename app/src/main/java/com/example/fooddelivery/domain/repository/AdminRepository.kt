package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.DashboardResponse

interface AdminRepository {
    suspend fun getDashboard(): Result<DashboardResponse>
}

