package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.AdminApi
import com.example.fooddelivery.data.remote.dto.AdminDashboardResponse
import com.example.fooddelivery.domain.repository.AdminRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AdminRepositoryImpl @Inject constructor(
    private val api: AdminApi
) : AdminRepository {

    override suspend fun getDashboard(): Result<AdminDashboardResponse> {
        return try {
            val response = api.getDashboard()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(e.localizedMessage ?: "Network error"))
        }
    }
}
