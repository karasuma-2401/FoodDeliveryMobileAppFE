package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceRequest(
    val deviceToken: String,
    val platform: String
)

@Serializable
data class DeviceResponse(
    val id: Int,
    val deviceToken: String,
    val platform: String,
    val userId: Int,
    val createdAt: String,
    val updatedAt: String
)
