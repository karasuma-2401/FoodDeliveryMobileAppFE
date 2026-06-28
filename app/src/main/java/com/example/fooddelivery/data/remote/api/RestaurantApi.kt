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
        @Query("categoryId") categoryId: Int? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("minRating") minRating: Double? = null,
        @Query("sortBy") sortBy: String? = null,
    ): Response<BaseResponse<List<RestaurantResponse>>>

    @GET("restaurant/detail/{id}")
    suspend fun getRestaurantById(@Path("id") id: Int): Response<BaseResponse<RestaurantResponse>>


    @GET("restaurant/my")
    suspend fun getMyRestaurants(): Response<BaseResponse<List<RestaurantResponse>>>

    @GET("restaurant/manage/{restaurantId}/dashboard")
    suspend fun getDashboard(
        @Path("restaurantId") restaurantId: Int,
        @Query("range") range: String = "day"
    ): Response<BaseResponse<RestaurantDashboardRangeResponse>>

    @GET("restaurant/generate-dashboard")
    suspend fun generateDashboard(
        @Query("restaurantId") restaurantId: Int
    ): Response<BaseResponse<RestaurantDashboardResponse>>


    @POST("restaurant/{restaurantId}/like")
    suspend fun toggleFavorite(@Path("restaurantId") restaurantId: Int): Response<BaseResponse<ToggleFavoritePayload>>

    @GET("restaurant/{restaurantId}/like-status")
    suspend fun getLikeStatus(@Path("restaurantId") restaurantId: Int): Response<BaseResponse<LikeStatusResponse>>

    @GET("food")
    suspend fun getFoods(
        @Query("restaurantId") restaurantId: Int,
        @Query("limit") limit: Int? = 100,
        @Query("offset") offset: Int? = 0
    ): Response<BaseResponse<List<FoodResponse>>>


    @Multipart
    @POST("food/manage")
    suspend fun addFood(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("categoryId") categoryId: RequestBody,
        @Part("restaurantId") restaurantId: RequestBody,
        @Part("price") price: RequestBody,
        @Part("sizes") sizes: RequestBody,
        @Part ingredientIds: List<MultipartBody.Part>?,
        @Part image: MultipartBody.Part?
    ): Response<BaseResponse<FoodResponse>>

    @GET("food/{id}")
    suspend fun getFoodById(@Path("id") id: Int): Response<BaseResponse<FoodResponse>>

    @Multipart
    @PATCH("food/manage/{id}")
    suspend fun updateFood(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("categoryId") categoryId: RequestBody,
        @Part("price") price: RequestBody,
        @Part("sizes") sizes: RequestBody,         // JSON string
        @Part("ingredientIds") ingredientIds: List<MultipartBody.Part>?,
        @Part image: MultipartBody.Part?
    ): Response<BaseResponse<FoodResponse>>

    @DELETE("food/manage/{id}")
    suspend fun deleteFood(@Path("id") id: Int): Response<BaseResponse<Unit>>

    @GET("restaurant/reviews/{restaurantId}")
    suspend fun getReviews(
        @Path("restaurantId") restaurantId: Int,
        @Query("limit") limit: Int? = 20,
        @Query("offset") offset: Int? = 0
    ): Response<BaseResponse<RestaurantReviewDataDto>>

    @POST("restaurant/reviews/{id}")
    suspend fun rateRestaurant(
        @Path("id") restaurantId: Int,
        @Body request: RestaurantRatingRequest
    ): Response<BaseResponse<FoodRatingResponse>>

    @GET("restaurant/manage/{restaurantId}/revenue-details")
    suspend fun getRestaurantRevenue(
        @Path("restaurantId") restaurantId: Int,
        @Query("limit") limit: Int? = 20,
        @Query("offset") offset: Int? = 0
    ): Response<BaseResponse<RevenueDataWrapperDto>>

    @PATCH("restaurant/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: Int,
        @Body request: UpdateReviewRequest
    ): Response<BaseResponse<UpdateReviewPayload>>

    @PATCH("restaurant/manage/{restaurantId}")
    suspend fun updateRestaurantProfile(
        @Path("restaurantId") restaurantId: Int,
        @Body request: UpdateRestaurantProfileRequest
    ): Response<BaseResponse<RestaurantResponse>>

    @Multipart
    @POST("restaurant/manage")
    suspend fun createRestaurant(
        @Part("name") name: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("description") description: RequestBody,
        @Part("addressId") addressId: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<BaseResponse<RestaurantResponse>>

    @POST("restaurant/business/register")
    suspend fun registerBusiness(): Response<BaseResponse<BusinessRegisterResponse>>
    @DELETE("restaurant/reviews/{reviewId}")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: Int
    ): Response<BaseResponse<Unit>>

    @POST("restaurant/reviews/{reviewId}/reply")
    suspend fun replyReview(
        @Path("reviewId") reviewId: Int,
        @Body request: ReplyReviewRequest
    ): Response<BaseResponse<Unit>>
}
