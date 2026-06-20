package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.DashboardResponse
import com.example.fooddelivery.data.remote.dto.FoodRequest
import com.example.fooddelivery.data.remote.dto.FoodResponse
import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.domain.model.Restaurant

interface RestaurantRepository {
    suspend fun getRestaurants(
        limit: Int? = 20,
        offset: Int? = 0,
        keyword: String? = null,
        categoryId: Int? = null
    ): Result<List<Restaurant>>

    suspend fun getDashboard(): Result<DashboardResponse>
    suspend fun getFoods(): Result<List<FoodResponse>>
    suspend fun addFood(request: FoodRequest): Result<BaseResponse<FoodResponse>>
    suspend fun getFoodById(id: String): Result<FoodResponse>
    suspend fun updateFood(id: String, request: FoodRequest): Result<BaseResponse<FoodResponse>>
    suspend fun deleteFood(id: String): Result<BaseResponse<Unit>>
}
