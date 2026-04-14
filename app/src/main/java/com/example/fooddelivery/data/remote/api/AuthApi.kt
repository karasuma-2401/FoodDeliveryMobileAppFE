package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.FacebookLoginRequest
import com.example.fooddelivery.data.remote.dto.LoginRequest
import com.example.fooddelivery.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login (@Body request: LoginRequest) : Response<LoginResponse>
    suspend fun loginFacebook (@Body request: FacebookLoginRequest) : Response<LoginResponse>
}