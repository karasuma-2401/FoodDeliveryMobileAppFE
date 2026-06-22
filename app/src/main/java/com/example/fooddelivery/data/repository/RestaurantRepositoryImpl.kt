package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.RestaurantApi
import com.example.fooddelivery.data.remote.dto.*
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.repository.RestaurantRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class RestaurantRepositoryImpl @Inject constructor(
    private val api: RestaurantApi
) : RestaurantRepository {

    override suspend fun getRestaurants(
        limit: Int?,
        offset: Int?,
        keyword: String?,
        categoryId: Int?
    ): Result<List<Restaurant>> {
        return try {
            val response = api.getRestaurants(limit, offset, keyword, categoryId)
            if (response.isSuccessful && response.body() != null) {
                val restaurants = response.body()!!.map { dto ->
                    Restaurant(
                        id = dto.id.toString(),
                        name = dto.name,
                        description = dto.description ?: "",
                        tags = dto.categories?.map { it.name } ?: emptyList(),
                        rating = dto.averageRating?.toFloat() ?: 0f,
                        deliveryFee = dto.deliveryFee ?: 0.0,
                        imageUrl = dto.image,
                        promoTags = if (dto.deliveryFee == 0.0) listOf("Free Delivery") else emptyList()
                    )
                }
                Result.success(restaurants)
            } else {
                Result.failure(Exception("Failed to load restaurants: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getDashboard(): Result<DashboardResponse> {
        return try {
            val response = api.getDashboard()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getFoods(): Result<List<FoodResponse>> {
        return try {
            val response = api.getFoods()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun addFood(request: FoodRequest): Result<BaseResponse<FoodResponse>> {
        return try {
            val response = api.addFood(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getFoodById(id: String): Result<FoodResponse> {
        return try {
            val response = api.getFoodById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun updateFood(id: String, request: FoodRequest): Result<BaseResponse<FoodResponse>> {
        return try {
            val response = api.updateFood(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun deleteFood(id: String): Result<BaseResponse<Unit>> {
        return try {
            val response = api.deleteFood(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun rateRestaurant(request: RestaurantRatingRequest): Result<FoodRatingResponse> {
        return try {
            val response = api.rateRestaurant(request.restaurantId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}
