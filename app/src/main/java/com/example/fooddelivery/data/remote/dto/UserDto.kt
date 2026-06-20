package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val name: String,
    val email: String,
    val phone: String,
    val birthday: String? = null,
    val avatar: String? = null
)
