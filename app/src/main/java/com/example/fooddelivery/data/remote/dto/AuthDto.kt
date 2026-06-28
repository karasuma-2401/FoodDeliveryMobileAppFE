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
    val idToken: String
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
data class VerifyResetOtpData(
    val resetToken: String
)

@Serializable
data class VerifyResetOtpResponse(
    val resetToken: String? = null,
    val success: Boolean? = null,
    val data: VerifyResetOtpData? = null
) {
    fun getFinalResetToken(): String? = resetToken ?: data?.resetToken
}

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
    val success: Boolean? = null,
    val data: AuthData? = null
) {
    fun getFinalToken(): String? = token ?: data?.token
    fun getFinalOtp(): String? = otp ?: data?.otp
    fun getFinalMessage(): String? = message ?: data?.message
}

@Serializable
data class AuthData(
    val token: String? = null,
    val otp: String? = null,
    val message: String? = null
)

@Serializable
data class RegisterResponse(
    val id: Int? = null,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val otp: String? = null,
    val success: Boolean? = null,
    val message: String? = null,
    val data: RegisterData? = null
) {
    fun getFinalId(): Int? = id ?: data?.id
    fun getFinalName(): String? = name ?: data?.name
    fun getFinalPhone(): String? = phone ?: data?.phone
    fun getFinalEmail(): String? = email ?: data?.email
    fun getFinalOtp(): String? = otp ?: data?.otp
}

@Serializable
data class RegisterData(
    val id: Int? = null,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
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

    @SerialName("access_token")
    val accessTokenSnake: String? = null,
    @SerialName("refresh_token")
    val refreshTokenSnake: String? = null,

    val data: LoginData? = null
) {
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

@Serializable
data class MeResponse(
    val id: Int? = null,
    val email: String? = null,
    val roles: List<String>? = null,
    val success: Boolean? = null,
    val message: String? = null,
    val data: MeData? = null
) {
    fun getFinalId(): Int? = id ?: data?.id
    fun getFinalEmail(): String? = email ?: data?.email
    fun getFinalRoles(): List<String> = roles ?: data?.roles ?: emptyList()
}

@Serializable
data class MeData(
    val id: Int? = null,
    val email: String? = null,
    val roles: List<String>? = null
)

typealias ForgotPasswordResponse = AuthResponse
typealias ResetPasswordResponse = AuthResponse
typealias ChangePasswordResponse = AuthResponse
typealias ResetEmailResponse = AuthResponse