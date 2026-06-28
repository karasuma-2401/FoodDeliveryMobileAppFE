package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.*
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.RestaurantListQuery
import com.example.fooddelivery.domain.model.RestaurantRevenue
import java.io.File

interface RestaurantRepository {

    suspend fun getRestaurants(query: RestaurantListQuery): Result<List<Restaurant>>

    suspend fun getRestaurantById(id: Int): Result<RestaurantResponse>

    suspend fun getMyRestaurants(): Result<List<RestaurantResponse>>

    suspend fun getDashboard(restaurantId: Int): Result<RestaurantDashboardRangeResponse>

    suspend fun generateDashboard(restaurantId: Int): Result<RestaurantDashboardResponse>

    suspend fun toggleFavorite(restaurantId: Int): Result<LikeStatusResponse>

    suspend fun getLikeStatus(restaurantId: Int): Result<LikeStatusResponse>

    suspend fun getFoods(restaurantId: Int): Result<List<FoodResponse>>

    suspend fun addFood(
        name: String,
        description: String,
        categoryId: Int,
        restaurantId: Int,
        price: Double,
        sizesJson: String,
        ingredientIds: List<Int>?,
        imageFile: File?
    ): Result<FoodResponse>

    suspend fun getFoodById(id: Int): Result<FoodResponse>

    suspend fun updateFood(
        id: Int,
        name: String,
        description: String,
        categoryId: Int,
        price: Double,
        sizesJson: String,
        ingredientIdsCsv: String?,
        imageFile: File?
    ): Result<FoodResponse>

    suspend fun deleteFood(id: Int): Result<Unit>
    suspend fun getRestaurantRevenue(restaurantId: Int): Result<RestaurantRevenue>
    suspend fun getRestaurantReviews(
        restaurantId: Int,
        limit: Int? = 20,
        offset: Int? = 0
    ): Result<List<VendorReviewResponse>>

    suspend fun rateRestaurant(
        restaurantId: Int,
        request: RestaurantRatingRequest
    ): Result<FoodRatingResponse>

    suspend fun updateReview(
        reviewId: Int,
        request: UpdateReviewRequest
    ): Result<FoodRatingResponse>

    suspend fun updateRestaurantProfile(
        restaurantId: Int,
        name: String,
        phone: String,
        description: String,
        addressId: Int?,
        image: File?
    ): Result<RestaurantResponse>

    suspend fun deleteReview(reviewId: Int): Result<Unit>
    suspend fun createRestaurant(
        name: String,
        phone: String,
        description: String,
        addressId: Int,
        image: File?
    ): Result<RestaurantResponse>

    suspend fun registerBusiness(): Result<BusinessRegisterResponse>
    suspend fun replyReview(reviewId: Int, reply: String): Result<Unit>
}
