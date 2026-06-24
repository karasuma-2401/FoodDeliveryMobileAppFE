package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.AuthResponse
import com.example.fooddelivery.data.remote.dto.LoginResponse
import com.example.fooddelivery.data.remote.dto.MeResponse
import com.example.fooddelivery.data.remote.dto.RegisterResponse
import com.example.fooddelivery.data.remote.dto.VerifyResetOtpResponse

interface AuthRepository {
    suspend fun login (phone: String, password: String) : Result<LoginResponse>
    suspend fun loginFacebook(accessToken: String? = null, code: String? = null) : Result<LoginResponse>
    suspend fun loginGoogle(accessToken: String? = null, code: String? = null) : Result<LoginResponse>
    suspend fun loginSocial(provider: String, accessToken: String? = null, code: String? = null) : Result<LoginResponse>
    suspend fun refreshToken(refreshToken: String) : Result<LoginResponse>
    suspend fun getMe(): Result<MeResponse>
    suspend fun register (name: String, email: String, phone: String, password: String, birthday: String? = null): Result<RegisterResponse>
    suspend fun verifyAccount(otp: String): Result<AuthResponse>
    suspend fun sendResetPasswordCode(email: String): Result<Unit>
    suspend fun verifyCode(email: String, code: String): Result<Unit>
    suspend fun verifyResetOtp(email: String, otp: String): Result<VerifyResetOtpResponse>
    suspend fun resetPassword (resetToken: String, newPassword: String) : Result<Unit>
    suspend fun changePassword(email: String?, phone: String?, currentPass: String, newPass: String): Result<Unit>
    suspend fun requestResetEmail(phone: String, password: String): Result<String?>
    suspend fun verifyResetEmail(newEmail: String, otp: String): Result<Unit>
}