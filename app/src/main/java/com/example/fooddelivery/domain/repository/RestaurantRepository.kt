package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.DashboardResponse
import com.example.fooddelivery.data.remote.dto.FoodRequest
import com.example.fooddelivery.data.remote.dto.FoodResponse
import com.example.fooddelivery.data.remote.dto.BaseResponse

interface RestaurantRepository {
    suspend fun getDashboard(): Result<DashboardResponse>
    suspend fun getFoods(): Result<List<FoodResponse>>
    suspend fun addFood(request: FoodRequest): Result<BaseResponse<FoodResponse>>
    suspend fun getFoodById(id: String): Result<FoodResponse>
    suspend fun updateFood(id: String, request: FoodRequest): Result<BaseResponse<FoodResponse>>
    suspend fun deleteFood(id: String): Result<BaseResponse<Unit>>
}
