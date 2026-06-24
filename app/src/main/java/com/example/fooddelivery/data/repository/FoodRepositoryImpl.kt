package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.FoodApi
import com.example.fooddelivery.data.remote.dto.FoodResponse
import com.example.fooddelivery.domain.repository.FoodRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class FoodRepositoryImpl @Inject constructor(
    private val foodApi: FoodApi
) : FoodRepository {

    override suspend fun getFoods(
        categoryId: Int?,
        restaurantId: Int?,
        keyword: String?
    ): Result<List<FoodResponse>> {
        return try {
            val response = foodApi.getFoods(categoryId, restaurantId, keyword)
            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                if (baseResponse.success == true && baseResponse.data != null) {
                    Result.success(baseResponse.data)
                } else {
                    Result.failure(Exception(baseResponse.message ?: "Failed to load foods"))
                }
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getFoodById(id: Int): Result<FoodResponse> {
        return try {
            val response = foodApi.getFoodById(id)
            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                if (baseResponse.success == true && baseResponse.data != null) {
                    Result.success(baseResponse.data)
                } else {
                    Result.failure(Exception(baseResponse.message ?: "Food does not exist"))
                }
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}