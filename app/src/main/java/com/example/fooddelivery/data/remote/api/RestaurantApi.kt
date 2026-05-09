package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.DashboardResponse
import com.example.fooddelivery.data.remote.dto.FoodRequest
import com.example.fooddelivery.data.remote.dto.FoodResponse
import retrofit2.Response
import retrofit2.http.*

interface RestaurantApi {
    @GET("restaurant/dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    @GET("restaurant/foods")
    suspend fun getFoods(): Response<List<FoodResponse>>

    @POST("restaurant/foods")
    suspend fun addFood(@Body request: FoodRequest): Response<BaseResponse<FoodResponse>>

    @GET("restaurant/foods/{id}")
    suspend fun getFoodById(@Path("id") id: String): Response<FoodResponse>

    @PUT("restaurant/foods/{id}")
    suspend fun updateFood(@Path("id") id: String, @Body request: FoodRequest): Response<BaseResponse<FoodResponse>>

    @DELETE("restaurant/foods/{id}")
    suspend fun deleteFood(@Path("id") id: String): Response<BaseResponse<Unit>>
}
