package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.FavoriteRestaurantResponse
import com.example.fooddelivery.data.remote.dto.UserProfileResponse
import com.example.fooddelivery.data.remote.dto.UserReviewDto
import com.example.fooddelivery.domain.model.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("user/profile")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    @PUT("user/profile")
    suspend fun updateUserProfile(@Body user: User): Response<Unit>

    @Multipart
    @POST("user/profile/image")
    suspend fun uploadProfileImage(@Part image: MultipartBody.Part): Response<String>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("user/reviews")
    suspend fun getUserReviews(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<UserReviewDto>>

    @GET("user/favorites/restaurants")
    suspend fun getFavoriteRestaurants(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<FavoriteRestaurantResponse>
}