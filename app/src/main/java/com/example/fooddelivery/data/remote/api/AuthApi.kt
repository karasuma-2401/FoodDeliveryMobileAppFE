package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.AuthResponse
import com.example.fooddelivery.data.remote.dto.FacebookLoginRequest
import com.example.fooddelivery.data.remote.dto.ForgotPasswordRequest
import com.example.fooddelivery.data.remote.dto.LoginRequest
import com.example.fooddelivery.data.remote.dto.RegisterRequest
import com.example.fooddelivery.data.remote.dto.ResetPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/login/facebook")
    suspend fun loginFacebook(@Body request: FacebookLoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("auth/reset-email/verify")
    suspend fun verifyCode(
        @Query("email") email: String,
        @Query("code") code: String
    ): Response<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<AuthResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<AuthResponse>
}