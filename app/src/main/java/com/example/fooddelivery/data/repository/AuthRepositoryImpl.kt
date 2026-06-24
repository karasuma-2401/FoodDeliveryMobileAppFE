package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.AuthApi
import com.example.fooddelivery.data.remote.dto.AuthResponse
import com.example.fooddelivery.data.remote.dto.ChangePasswordRequest
import com.example.fooddelivery.data.remote.dto.FacebookLoginRequest
import com.example.fooddelivery.data.remote.dto.GoogleLoginRequest
import com.example.fooddelivery.data.remote.dto.ForgotPasswordRequest
import com.example.fooddelivery.data.remote.dto.LoginRequest
import com.example.fooddelivery.data.remote.dto.LoginResponse
import com.example.fooddelivery.data.remote.dto.MeResponse
import com.example.fooddelivery.data.remote.dto.RefreshRequest
import com.example.fooddelivery.data.remote.dto.RegisterRequest
import com.example.fooddelivery.data.remote.dto.RegisterResponse
import com.example.fooddelivery.data.remote.dto.ResetEmailRequest
import com.example.fooddelivery.data.remote.dto.ResetPasswordRequest
import com.example.fooddelivery.data.remote.dto.SocialLoginRequest
import com.example.fooddelivery.data.remote.dto.VerifyCodeRequest
import com.example.fooddelivery.data.remote.dto.VerifyResetOtpRequest
import com.example.fooddelivery.data.remote.dto.VerifyResetOtpResponse
import com.example.fooddelivery.domain.repository.AuthRepository
import com.example.fooddelivery.domain.exception.UnauthorizedException
import com.example.fooddelivery.domain.exception.UserNotFoundException
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
                val errorMsg = response.errorBody()?.string() ?: "Login failed: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun loginFacebook(accessToken: String?, code: String?): Result<LoginResponse> {
        return try {
            val response = api.loginFacebook(FacebookLoginRequest(accessToken = accessToken, code = code))
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Login with facebook failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun loginGoogle(accessToken: String?, code: String?): Result<LoginResponse> {
        return try {
            val response = api.loginGoogle(GoogleLoginRequest(accessToken = accessToken, code = code))
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Login with google failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun loginSocial(
        provider: String,
        accessToken: String?,
        code: String?
    ): Result<LoginResponse> {
        return try {
            val request = SocialLoginRequest(provider, accessToken, code)
            val response = api.loginSocial(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Social login failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
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
                val errorMsg = response.errorBody()?.string() ?: "Refresh token failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun getMe(): Result<MeResponse> {
        return try {
            val response = api.getMe()
            when {
                response.isSuccessful -> {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response body"))
                }
                response.code() == 401 -> {
                    Result.failure(UnauthorizedException("Token không hợp lệ hoặc đã hết hạn"))
                }
                response.code() == 404 -> {
                    Result.failure(UserNotFoundException("Tài khoản không tồn tại hoặc chưa được kích hoạt"))
                }
                else -> {
                    val errorMsg = response.errorBody()?.string() ?: "Get me failed: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun register(name: String, email: String, phone: String, password: String, birthday: String?): Result<RegisterResponse> {
        return try {
            val request = RegisterRequest(name, email, phone, password, birthday)
            val response = api.register(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Registration failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun verifyAccount(otp: String): Result<AuthResponse> {
        return try {
            val response = api.verifyAccount(otp)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Verification failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun sendResetPasswordCode(email: String): Result<Unit> {
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to send reset code"
                Result.failure(Exception(errorMsg))
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

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to verify code"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error, please try again. ${e.localizedMessage}"))
        }
    }

    override suspend fun verifyResetOtp(email: String, otp: String): Result<VerifyResetOtpResponse> {
        return try {
            val request = VerifyResetOtpRequest(email, otp)
            val response = api.verifyResetOtp(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to verify OTP"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun resetPassword(
        resetToken: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val request = ResetPasswordRequest(resetToken, newPassword)
            val response = api.resetPassword(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to reset password"
                Result.failure(Exception(errorMsg))
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
                val errorMsg = response.errorBody()?.string() ?: "Change password failed"
                Result.failure(Exception(errorMsg))
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
                val errorMsg = response.errorBody()?.string() ?: "Request failed"
                Result.failure(Exception(errorMsg))
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
                val errorMsg = response.errorBody()?.string() ?: "Verification failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }
}