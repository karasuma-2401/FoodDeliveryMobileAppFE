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
data class LoginResponse (
    val token: String? = null,
    val message: String? = null,
    val isSuccess: Boolean,
)