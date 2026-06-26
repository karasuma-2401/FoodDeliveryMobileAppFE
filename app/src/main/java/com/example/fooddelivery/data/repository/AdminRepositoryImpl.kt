package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.AdminApi
import com.example.fooddelivery.data.remote.dto.AdminDashboardResponse
import com.example.fooddelivery.data.remote.dto.AdminPaymentDto
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.domain.repository.AdminRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import com.example.fooddelivery.data.remote.dto.AdminUserListResponse
import com.example.fooddelivery.data.remote.dto.AdminRevenueDataDto
import com.example.fooddelivery.data.remote.dto.RestaurantItemDto
import com.example.fooddelivery.data.remote.dto.ApprovalRequest
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
    override suspend fun getAdminRevenue(): Result<AdminRevenueDataDto> {
        return try {
            api.getAdminRevenue().unwrapData("Failed to load revenue analytics")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(e.localizedMessage ?: "Network error"))
        }
    }
    override suspend fun getMyRestaurants(): Result<List<RestaurantItemDto>> {
        return try {
            api.getMyRestaurants().unwrapData("Failed to fetch restaurants")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRestaurantApproval(restaurantId: Int, status: String): Result<Unit> {
        return try {
            val response = api.updateRestaurantApproval(restaurantId, ApprovalRequest(status))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Action failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
