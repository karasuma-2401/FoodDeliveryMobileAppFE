package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.FoodApi
import com.example.fooddelivery.data.remote.dto.FoodResponse
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.domain.repository.FoodRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import com.example.fooddelivery.data.remote.dto.IngredientDto

class FoodRepositoryImpl @Inject constructor(
    private val foodApi: FoodApi
) : FoodRepository {

    override suspend fun getFoods(
        categoryId: Int?,
        restaurantId: Int?,
        keyword: String?
    ): Result<List<FoodResponse>> {
        return try {
            foodApi.getFoods(categoryId, restaurantId, keyword)
                .unwrapData("Failed to load foods")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getFoodById(id: Int): Result<FoodResponse> {
        return try {
            foodApi.getFoodById(id).unwrapData("Food does not exist")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
    override suspend fun getIngredients(): Result<List<IngredientDto>> {
        return try {
            foodApi.getIngredients().unwrapData("Failed to load ingredients")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}
