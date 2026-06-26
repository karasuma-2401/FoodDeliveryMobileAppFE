package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.AdminDashboardResponse
import com.example.fooddelivery.data.remote.dto.AdminPaymentDto
import com.example.fooddelivery.data.remote.dto.BaseResponse
import retrofit2.Response
import retrofit2.http.GET

interface AdminApi {
    @GET("admin/dashboard")
    suspend fun getDashboard(): Response<BaseResponse<AdminDashboardResponse>>

    @GET("admin/payments")
    suspend fun getAdminPayments(): Response<BaseResponse<List<AdminPaymentDto>>>
}
