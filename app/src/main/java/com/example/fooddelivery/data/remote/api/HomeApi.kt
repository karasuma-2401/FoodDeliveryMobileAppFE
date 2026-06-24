package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.HomeDashboardDto
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApi {
    @GET("home/dashboard")
    suspend fun getDashboard(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null
    ): BaseResponse<HomeDashboardDto>
}
