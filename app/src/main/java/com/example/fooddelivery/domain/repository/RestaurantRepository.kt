package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.*
import com.example.fooddelivery.domain.model.Restaurant
import java.io.File

interface RestaurantRepository {

    suspend fun getRestaurants(
        limit: Int? = 20,
        offset: Int? = 0,
        keyword: String? = null,
        categoryId: Int? = null
    ): Result<List<Restaurant>>

    suspend fun getMyRestaurants(): Result<List<RestaurantResponse>>

    suspend fun getDashboard(restaurantId: Int): Result<DashboardResponse>

    suspend fun getFoods(restaurantId: Int): Result<List<FoodResponse>>

    suspend fun addFood(
        name: String,
        description: String,
        categoryId: Int,
        restaurantId: Int,
        price: Double,
        sizesJson: String,
        ingredientIdsCsv: String?,
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

    suspend fun rateRestaurant(
        restaurantId: Int,
        request: RestaurantRatingRequest
    ): Result<FoodRatingResponse>

    suspend fun updateReview(
        reviewId: Int,
        request: UpdateReviewRequest
    ): Result<FoodRatingResponse>

    suspend fun deleteReview(reviewId: Int): Result<FoodRatingResponse>

}
