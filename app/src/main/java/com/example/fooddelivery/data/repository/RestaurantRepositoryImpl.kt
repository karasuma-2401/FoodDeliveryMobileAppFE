package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.RestaurantApi
import com.example.fooddelivery.data.remote.dto.*
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.repository.RestaurantRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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
                // 🌟 Bóc tách .data từ BaseListResponse
                val baseResponse = response.body()!!
                val restaurants = baseResponse.data?.map { dto ->
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
                } ?: emptyList()
                Result.success(restaurants)
            } else {
                Result.failure(Exception("Failed to load restaurants: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getMyRestaurants(): Result<List<RestaurantResponse>> {
        return try {
            val response = api.getMyRestaurants()
            if (response.isSuccessful && response.body() != null) {
                // 🌟 Bóc tách .data từ BaseListResponse tại đây
                val baseResponse = response.body()!!
                Result.success(baseResponse.data ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load my restaurants: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getDashboard(restaurantId: Int): Result<DashboardResponse> {
        return try {
            val response = api.getDashboard(restaurantId)
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

    override suspend fun getFoods(restaurantId: Int): Result<List<FoodResponse>> {
        return try {
            val response = api.getFoods(restaurantId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Failed to load foods: data is null"))
                }
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun addFood(
        name: String,
        description: String,
        categoryId: Int,
        restaurantId: Int,
        price: Double,
        sizesJson: String,
        ingredientIdsCsv: String?,
        imageFile: File?
    ): Result<FoodResponse> {
        return try {
            val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryIdBody = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val restaurantIdBody = restaurantId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val sizesBody = sizesJson.toRequestBody("application/json".toMediaTypeOrNull())
            val ingredientIdsBody = ingredientIdsCsv?.toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", it.name, requestFile)
            }

            val response = api.addFood(
                name = nameBody,
                description = descriptionBody,
                categoryId = categoryIdBody,
                restaurantId = restaurantIdBody,
                price = priceBody,
                sizes = sizesBody,
                ingredientIds = ingredientIdsBody,
                image = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to add food: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getFoodById(id: Int): Result<FoodResponse> {
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

    override suspend fun updateFood(
        id: Int,
        name: String,
        description: String,
        categoryId: Int,
        price: Double,
        sizesJson: String,
        ingredientIdsCsv: String?,
        imageFile: File?
    ): Result<FoodResponse> {
        return try {
            val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryIdBody = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val sizesBody = sizesJson.toRequestBody("application/json".toMediaTypeOrNull())
            val ingredientIdsBody = ingredientIdsCsv?.toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", it.name, requestFile)
            }

            val response = api.updateFood(
                id = id,
                name = nameBody,
                description = descriptionBody,
                categoryId = categoryIdBody,
                price = priceBody,
                sizes = sizesBody,
                ingredientIds = ingredientIdsBody,
                image = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update food: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun deleteFood(id: Int): Result<Unit> {
        return try {
            val response = api.deleteFood(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete food: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun rateRestaurant(restaurantId: Int, request: RestaurantRatingRequest): Result<FoodRatingResponse> {
        return try {
            val response = api.rateRestaurant(restaurantId, request)
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

    override suspend fun updateReview(reviewId: Int, request: UpdateReviewRequest): Result<FoodRatingResponse> {
        return try {
            val response = api.updateReview(reviewId, request)
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

    override suspend fun deleteReview(reviewId: Int): Result<FoodRatingResponse> {
        return try {
            val response = api.deleteReview(reviewId)
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
