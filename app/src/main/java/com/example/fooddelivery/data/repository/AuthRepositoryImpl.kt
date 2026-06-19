package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.AuthApi
import com.example.fooddelivery.data.remote.dto.ChangePasswordRequest
import com.example.fooddelivery.data.remote.dto.FacebookLoginRequest
import com.example.fooddelivery.data.remote.dto.GoogleLoginRequest
import com.example.fooddelivery.data.remote.dto.ForgotPasswordRequest
import com.example.fooddelivery.data.remote.dto.LoginRequest
import com.example.fooddelivery.data.remote.dto.LoginResponse
import com.example.fooddelivery.data.remote.dto.RefreshRequest
import com.example.fooddelivery.data.remote.dto.RegisterRequest
import com.example.fooddelivery.data.remote.dto.RegisterResponse
import com.example.fooddelivery.data.remote.dto.ResetEmailRequest
import com.example.fooddelivery.data.remote.dto.ResetPasswordRequest
import com.example.fooddelivery.data.remote.dto.VerifyCodeRequest
import com.example.fooddelivery.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi
) : AuthRepository {
    override suspend fun login(phone: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(phone, password)
            val response = api.login(request)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun loginFacebook(facebookToken: String): Result<LoginResponse> {
        return try {
            val response = api.loginFacebook(FacebookLoginRequest(accessToken = facebookToken))
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Login with facebook failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun loginGoogle(googleToken: String): Result<LoginResponse> {
        return try {
            val response = api.loginGoogle(GoogleLoginRequest(accessToken = googleToken))
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Login with google failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<LoginResponse> {
        return try {
            val response = api.refreshToken(RefreshRequest(refreshToken))
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Refresh token failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun register(name: String, email: String, phone: String, password: String): Result<RegisterResponse> {
        return try {
            val request = RegisterRequest(name, email, phone, password)
            val response = api.register(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun verifyAccount(otp: String): Result<Unit> {
        return try {
            val response = api.verifyAccount(otp)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Verification failed: ${response.code()}"))
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
            val request = VerifyCodeRequest(email, code)
            val response = api.verifyCode(request)

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

    override suspend fun changePassword(
        email: String?,
        phone: String?,
        currentPass: String,
        newPass: String
    ): Result<Unit> {
        return try {
            val request = ChangePasswordRequest(email, phone, currentPass, newPass)
            val response = api.changePassword(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Change password failed"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun requestResetEmail(phone: String, password: String): Result<String?> {
        return try {
            val response = api.requestResetEmail(ResetEmailRequest(phone, password))
            if (response.isSuccessful) {
                Result.success(response.body()?.otp)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Request failed"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun verifyResetEmail(newEmail: String, otp: String): Result<Unit> {
        return try {
            val response = api.verifyResetEmail(newEmail, otp)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Verification failed"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }
}
