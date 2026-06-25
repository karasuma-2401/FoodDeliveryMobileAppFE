package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.RestaurantApi
import com.example.fooddelivery.data.remote.dto.*
import com.example.fooddelivery.data.remote.parseErrorMessage
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.data.remote.unwrapList
import com.example.fooddelivery.data.remote.unwrapUnit
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.repository.RestaurantRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
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
            api.getRestaurants(limit, offset, keyword, categoryId)
                .unwrapList("Failed to load restaurants")
                .map { list ->
                    list.map { dto ->
                        Restaurant(
                            id = dto.id.toString(),
                            name = dto.name,
                            description = dto.description ?: "",
                            tags = dto.categories?.map { it.name } ?: emptyList(),
                            rating = dto.averageRating?.toFloat() ?: 0f,
                            deliveryFee = dto.deliveryFee ?: 0.0,
                            imageUrl = dto.image,
                            promoTags = if (dto.deliveryFee == 0.0) listOf("Free Delivery") else emptyList(),
                            isLiked = dto.isLiked ?: false,
                            totalLikes = dto.totalLikes ?: 0
                        )
                    }
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getRestaurantById(id: Int): Result<RestaurantResponse> {
        return try {
            api.getRestaurantById(id).unwrapData("Failed to load restaurant details")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getMyRestaurants(): Result<List<RestaurantResponse>> {
        return try {
            api.getMyRestaurants().unwrapList("Failed to load my restaurants")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getDashboard(restaurantId: Int): Result<RestaurantDashboardRangeResponse> {
        return try {
            api.getDashboard(restaurantId).unwrapData("Failed to load dashboard")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun generateDashboard(restaurantId: Int): Result<RestaurantDashboardResponse> {
        return try {
            api.generateDashboard(restaurantId).unwrapData("Failed to generate dashboard")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(restaurantId: Int): Result<LikeStatusResponse> {
        return try {
            val response = api.toggleFavorite(restaurantId)
            val result = response.unwrapData("Failed to update favorite status")
            if (result.isSuccess) {
                result
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Unauthorized: Please login again"
                    403 -> "Forbidden: You don't have permission"
                    404 -> "Restaurant not found"
                    else -> response.parseErrorMessage("Failed to update favorite status")
                }
                Result.failure(Exception(errorMsg, result.exceptionOrNull()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getLikeStatus(restaurantId: Int): Result<LikeStatusResponse> {
        return try {
            api.getLikeStatus(restaurantId).unwrapData("Failed to get like status")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getFoods(restaurantId: Int): Result<List<FoodResponse>> {
        return try {
            api.getFoods(restaurantId).unwrapList("Failed to load foods")
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
            val response = api.addFood(
                name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                categoryId = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                restaurantId = restaurantId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                price = price.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                sizes = sizesJson.toRequestBody("application/json".toMediaTypeOrNull()),
                ingredientIds = ingredientIdsCsv?.toRequestBody("text/plain".toMediaTypeOrNull()),
                image = imageFile?.let {
                    val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", it.name, requestFile)
                }
            )
            response.unwrapData("Failed to add food")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getFoodById(id: Int): Result<FoodResponse> {
        return try {
            api.getFoodById(id).unwrapData("Failed to load food")
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
            val response = api.updateFood(
                id = id,
                name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                categoryId = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                price = price.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                sizes = sizesJson.toRequestBody("application/json".toMediaTypeOrNull()),
                ingredientIds = ingredientIdsCsv?.toRequestBody("text/plain".toMediaTypeOrNull()),
                image = imageFile?.let {
                    val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", it.name, requestFile)
                }
            )
            response.unwrapData("Failed to update food")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun deleteFood(id: Int): Result<Unit> {
        return try {
            api.deleteFood(id).unwrapUnit("Failed to delete food")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun rateRestaurant(restaurantId: Int, request: RestaurantRatingRequest): Result<FoodRatingResponse> {
        return try {
            api.rateRestaurant(restaurantId, request).unwrapData("Failed to rate restaurant")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getRestaurantReviews(restaurantId: Int): Result<List<RestaurantReviewDto>> {
        return try {
            api.getRestaurantReviews(restaurantId).unwrapData("Failed to load restaurant reviews")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun updateReview(reviewId: Int, request: UpdateReviewRequest): Result<FoodRatingResponse> {
        return try {
            api.updateReview(reviewId, request).unwrapData("Failed to update review")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun deleteReview(reviewId: Int): Result<FoodRatingResponse> {
        return try {
            api.deleteReview(reviewId).unwrapData("Failed to delete review")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}
