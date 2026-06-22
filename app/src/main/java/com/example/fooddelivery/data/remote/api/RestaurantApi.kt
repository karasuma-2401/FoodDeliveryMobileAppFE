package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.*
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

    @GET("restaurant/dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    @GET("restaurant/foods")
    suspend fun getFoods(): Response<List<FoodResponse>>

    @POST("restaurant/foods")
    suspend fun addFood(@Body request: FoodRequest): Response<BaseResponse<FoodResponse>>

    @GET("restaurant/foods/{id}")
    suspend fun getFoodById(@Path("id") id: String): Response<FoodResponse>

    @PUT("restaurant/foods/{id}")
    suspend fun updateFood(@Path("id") id: String, @Body request: FoodRequest): Response<BaseResponse<FoodResponse>>

    @DELETE("restaurant/foods/{id}")
    suspend fun deleteFood(@Path("id") id: String): Response<BaseResponse<Unit>>

    @POST("api/restaurant/{id}/ratings")
    suspend fun rateRestaurant(
        @Path("id") restaurantId: Int,
        @Body request: RestaurantRatingRequest
    ): Response<FoodRatingResponse>
}
