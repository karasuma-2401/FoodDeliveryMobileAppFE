package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.AuthResponse
import com.example.fooddelivery.data.remote.dto.ChangePasswordRequest
import com.example.fooddelivery.data.remote.dto.FacebookLoginRequest
import com.example.fooddelivery.data.remote.dto.GoogleLoginRequest
import com.example.fooddelivery.data.remote.dto.ForgotPasswordRequest
import com.example.fooddelivery.data.remote.dto.LoginRequest
import com.example.fooddelivery.data.remote.dto.LoginResponse
import com.example.fooddelivery.data.remote.dto.RefreshRequest
import com.example.fooddelivery.data.remote.dto.RegisterRequest
import com.example.fooddelivery.data.remote.dto.RegisterResponse
import com.example.fooddelivery.data.remote.dto.ResetPasswordRequest
import com.example.fooddelivery.data.remote.dto.VerifyCodeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/login-facebook")
    suspend fun loginFacebook(@Body request: FacebookLoginRequest): Response<LoginResponse>

    @POST("auth/login-google")
    suspend fun loginGoogle(@Body request: GoogleLoginRequest): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("auth/verify")
    suspend fun verifyAccount(@Query("otp") otp: String): Response<AuthResponse>

    @POST("auth/reset-email/verify")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): Response<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<AuthResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<AuthResponse>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<AuthResponse>
}