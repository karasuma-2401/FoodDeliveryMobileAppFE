package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadImageResponse(
    val imageUrl: String
)
