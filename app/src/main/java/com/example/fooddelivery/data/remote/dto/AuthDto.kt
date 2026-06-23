package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LoginRequest (
    val phone: String,
    val password: String,
)

@Serializable
data class FacebookLoginRequest (
    val accessToken: String? = null,
    val code: String? = null
)

@Serializable
data class GoogleLoginRequest (
    val accessToken: String? = null,
    val code: String? = null
)

@Serializable
data class SocialLoginRequest(
    val provider: String,
    val accessToken: String? = null,
    val code: String? = null
)

@Serializable
data class RefreshRequest (
    val refreshToken: String
)

@Serializable
data class ChangePasswordRequest(
    val email: String? = null,
    val phone: String? = null,
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class ResetEmailRequest(
    val phone: String,
    val password: String
)

@Serializable
data class RegisterRequest (
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val birthday: String? = null
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class VerifyCodeRequest(
    val email: String,
    val code: String
)

@Serializable
data class VerifyResetOtpRequest(
    val email: String,
    val otp: String
)

@Serializable
data class VerifyResetOtpResponse(
    val resetToken: String
)

@Serializable
data class ResetPasswordRequest(
    val resetToken: String,
    val newPassword: String
)

@Serializable
data class AuthResponse (
    val token: String? = null,
    val otp: String? = null,
    val message: String? = null,
    val isSuccess: Boolean = false,
)

@Serializable
data class RegisterResponse(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String,
    val otp: String? = null
)

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    val birthday: String? = null,
    val avatar: String? = null,
    val active: Boolean = true,
    val roles: List<String> = emptyList()
)

@Serializable
data class LoginResponse(
    @SerialName("accessToken")
    val accessToken: String? = null,
    @SerialName("refreshToken")
    val refreshToken: String? = null,
    @SerialName("user")
    val user: UserDto? = null,
    
    // Hỗ trợ nếu BE trả về snake_case
    @SerialName("access_token")
    val accessTokenSnake: String? = null,
    @SerialName("refresh_token")
    val refreshTokenSnake: String? = null,

    // Hỗ trợ nếu BE bọc trong object "data"
    val data: LoginData? = null
) {
    // Helper để lấy token dù BE trả về kiểu gì
    fun getFinalAccessToken(): String? = accessToken ?: accessTokenSnake ?: data?.accessToken ?: data?.accessTokenSnake
    fun getFinalRefreshToken(): String? = refreshToken ?: refreshTokenSnake ?: data?.refreshToken ?: data?.refreshTokenSnake
    fun getFinalUser(): UserDto? = user ?: data?.user
}

@Serializable
data class LoginData(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    @SerialName("access_token")
    val accessTokenSnake: String? = null,
    @SerialName("refresh_token")
    val refreshTokenSnake: String? = null,
    val user: UserDto? = null
)

typealias ForgotPasswordResponse = AuthResponse
typealias ResetPasswordResponse = AuthResponse
typealias ChangePasswordResponse = AuthResponse
typealias ResetEmailResponse = AuthResponse