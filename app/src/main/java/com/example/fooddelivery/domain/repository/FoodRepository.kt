package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.FoodResponse

interface FoodRepository {
    suspend fun getFoods(
        categoryId: Int? = null,
        restaurantId: Int? = null,
        keyword: String? = null
    ): Result<List<FoodResponse>>
    
    suspend fun getFoodById(id: Int): Result<FoodResponse>
}
