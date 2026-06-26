package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.AdminDashboardResponse
import com.example.fooddelivery.data.remote.dto.AdminPaymentDto
import com.example.fooddelivery.data.remote.dto.AdminUserListResponse
import com.example.fooddelivery.data.remote.dto.BaseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface AdminApi {
    @GET("admin/dashboard")
    suspend fun getDashboard(): Response<BaseResponse<AdminDashboardResponse>>

    @GET("admin/payments")
    suspend fun getAdminPayments(): Response<BaseResponse<List<AdminPaymentDto>>>

    @GET("admin/users")
    suspend fun getAdminUsers(): Response<BaseResponse<AdminUserListResponse>>

    @PUT("admin/users/{id}/toggle-active")
    suspend fun toggleUserActive(
        @Path("id") userId: Int
    ): Response<BaseResponse<Unit>>
}
