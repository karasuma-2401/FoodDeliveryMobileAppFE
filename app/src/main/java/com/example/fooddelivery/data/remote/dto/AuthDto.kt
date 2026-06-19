package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

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
data class RegisterRequest (
    val name: String,
    val email: String,
    val phone: String,
    val password: String
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
data class ResetPasswordRequest(
    val email: String,
    val resetCode: String,
    val newPass: String
)

@Serializable
data class AuthResponse (
    val token: String? = null,
    val message: String? = null,
    val isSuccess: Boolean = false,
)

@Serializable
data class RegisterResponse(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String
)

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val birthday: String? = null,
    val avatar: String? = null,
    val active: Boolean,
    val roles: List<String>
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

typealias ForgotPasswordResponse = AuthResponse
typealias ResetPasswordResponse = AuthResponse
typealias ChangePasswordResponse = AuthResponse