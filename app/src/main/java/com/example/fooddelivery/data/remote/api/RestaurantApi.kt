package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface RestaurantApi {
    @GET("restaurant")
    suspend fun getRestaurants(
        @Query("limit") limit: Int? = 20,
        @Query("offset") offset: Int? = 0,
        @Query("keyword") keyword: String? = null,
        @Query("categoryId") categoryId: Int? = null
    ): Response<List<RestaurantResponse>>

    @GET("restaurant/my")
    suspend fun getMyRestaurants(): Response<List<RestaurantResponse>>

    @GET("restaurant/manage/{restaurantId}/dashboard")
    suspend fun getDashboard(
        @Path("restaurantId") restaurantId: Int,
        @Query("range") range: String = "day"
    ): Response<DashboardResponse>

    @GET("food")
    suspend fun getFoods(
        @Query("restaurantId") restaurantId: Int
    ): Response<List<FoodResponse>>

    @Multipart
    @POST("food/manage")
    suspend fun addFood(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("categoryId") categoryId: RequestBody,
        @Part("restaurantId") restaurantId: RequestBody,
        @Part("price") price: RequestBody,
        @Part("sizes") sizes: RequestBody,         // JSON string
        @Part("ingredientIds") ingredientIds: RequestBody?, // CSV string
        @Part image: MultipartBody.Part?
    ): Response<FoodResponse>

    @GET("food/{id}")
    suspend fun getFoodById(@Path("id") id: Int): Response<FoodResponse>

    @Multipart
    @PATCH("food/manage/{id}")
    suspend fun updateFood(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("categoryId") categoryId: RequestBody,
        @Part("price") price: RequestBody,
        @Part("sizes") sizes: RequestBody,         // JSON string
        @Part("ingredientIds") ingredientIds: RequestBody?, // CSV string
        @Part image: MultipartBody.Part?
    ): Response<FoodResponse>

    @DELETE("food/manage/{id}")
    suspend fun deleteFood(@Path("id") id: Int): Response<Unit>

    @POST("api/restaurant/{id}/ratings")
    suspend fun rateRestaurant(
        @Path("id") restaurantId: Int,
        @Body request: RestaurantRatingRequest
    ): Response<FoodRatingResponse>
}
