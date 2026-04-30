package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest (
    val phone: String,
    val password: String,
)

@Serializable
data class FacebookLoginRequest (
    val accessToken: String
)

@Serializable
data class RegisterRequest (
    val fullName: String,
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
    val isSuccess: Boolean,
)

typealias LoginResponse = AuthResponse
typealias RegisterResponse = AuthResponse
typealias ForgotPasswordResponse = AuthResponse
typealias ResetPasswordResponse = AuthResponse