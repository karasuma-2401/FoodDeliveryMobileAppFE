package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.AdminApi
import com.example.fooddelivery.data.remote.dto.AdminDashboardResponse
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.domain.repository.AdminRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AdminRepositoryImpl @Inject constructor(
    private val api: AdminApi
) : AdminRepository {

    override suspend fun getDashboard(): Result<AdminDashboardResponse> {
        return try {
            api.getDashboard().unwrapData("Failed to load admin dashboard")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(e.localizedMessage ?: "Network error"))
        }
    }
}
