package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.FavoriteRestaurantResponse
import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.UserProfileResponse
import com.example.fooddelivery.data.remote.dto.UserReviewDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("user/profile")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    @Multipart
    @PUT("user/profile")
    suspend fun updateUserProfile(
        @Part("name") name: RequestBody? = null,
        @Part("phone") phone: RequestBody? = null,
        @Part("birthday") birthday: RequestBody? = null,
        @Part avatar: MultipartBody.Part? = null
    ): Response<UserProfileResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("user/reviews")
    suspend fun getUserReviews(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<BaseResponse<List<UserReviewDto>>>

    @GET("user/favorites/restaurants")
    suspend fun getFavoriteRestaurants(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<BaseResponse<FavoriteRestaurantResponse>>
}
