package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.AdminApi
import com.example.fooddelivery.data.remote.dto.AdminDashboardResponse
import com.example.fooddelivery.data.remote.dto.AdminPaymentDto
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.domain.repository.AdminRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import com.example.fooddelivery.data.remote.dto.AdminUserListResponse
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
    override suspend fun getAdminPayments(): Result<List<AdminPaymentDto>> {
        return try {
            api.getAdminPayments().unwrapData("Failed to load admin payments")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(e.localizedMessage ?: "Network error"))
        }
    }
    override suspend fun getAdminUsers(): Result<AdminUserListResponse> {
        return try {
            api.getAdminUsers().unwrapData("Failed to load user list")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(e.localizedMessage ?: "Network error"))
        }
    }

    override suspend fun toggleUserActive(userId: Int): Result<Unit> {
        return try {
            api.toggleUserActive(userId).unwrapData("Failed to update status")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(e.localizedMessage ?: "System error"))
        }
    }
}
