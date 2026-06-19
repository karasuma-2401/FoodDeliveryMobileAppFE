package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.LoginResponse
import com.example.fooddelivery.data.remote.dto.RegisterResponse

interface AuthRepository {
    suspend fun login (phone: String, password: String) : Result<LoginResponse>
    suspend fun loginFacebook(facebookToken: String) : Result<LoginResponse>
    suspend fun loginGoogle(googleToken: String) : Result<LoginResponse>
    suspend fun refreshToken(refreshToken: String) : Result<LoginResponse>
    suspend fun register (name: String, email: String, phone: String, password: String): Result<RegisterResponse>
    suspend fun verifyAccount(otp: String): Result<Unit>
    suspend fun sendResetPasswordCode(email: String): Result<Unit>
    suspend fun verifyCode(email: String, code: String): Result<Unit>
    suspend fun resetPassword (email: String, resetCode: String, newPassword: String) : Result<Unit>
}