package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.domain.model.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("user/profile")
    suspend fun getUserProfile(): Response<User>

    @PUT("user/profile")
    suspend fun updateUserProfile(@Body user: User): Response<Unit>

    @Multipart
    @POST("user/profile/image")
    suspend fun uploadProfileImage(@Part image: MultipartBody.Part): Response<String>
}