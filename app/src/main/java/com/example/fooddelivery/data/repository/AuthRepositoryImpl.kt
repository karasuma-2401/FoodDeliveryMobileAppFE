package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.AuthApi
import com.example.fooddelivery.data.remote.dto.FacebookLoginRequest
import com.example.fooddelivery.data.remote.dto.ForgotPasswordRequest
import com.example.fooddelivery.data.remote.dto.LoginRequest
import com.example.fooddelivery.data.remote.dto.RegisterRequest
import com.example.fooddelivery.data.remote.dto.ResetPasswordRequest
import com.example.fooddelivery.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi
) : AuthRepository {
    override suspend fun login(phone: String, password: String): Result<String> {
        return try {
            val request = LoginRequest(phone, password)
            val response = api.login(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess == true && body.token != null) {
                    Result.success(body.token)
                } else {
                    Result.failure(Exception(body?.message ?: "Login failed from server"))
                }
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun loginFacebook(facebookToken: String): Result<String> {
        return try {
            val response = api.loginFacebook(FacebookLoginRequest(facebookToken))
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                val token = response.body()?.token
                if (token != null) {
                    Result.success(token)
                } else {
                    Result.failure(Exception("Server response success without token"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login with facebook failed"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun register(fullName: String, email: String, phone: String, password: String): Result<String> {
        return try {
            val request = RegisterRequest(fullName, email, phone, password)
            val response = api.register(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess == true && body.token != null) {
                    Result.success(body.token)
                } else {
                    Result.failure(Exception(body?.message ?: "Register failed from server"))
                }
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun sendResetPasswordCode(email: String): Result<Unit> {
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to send reset code"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun verifyCode(email: String, code: String): Result<Unit> {
        return try {
            val response = api.verifyCode(email, code)
            
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to verify code"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun resetPassword(
        email: String,
        resetCode: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val request = ResetPasswordRequest(email, resetCode, newPassword)
            val response = api.resetPassword(request)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to reset password"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }
}
