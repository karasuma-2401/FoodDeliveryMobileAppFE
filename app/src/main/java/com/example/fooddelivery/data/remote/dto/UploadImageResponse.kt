package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadImageResponse(
    val imageUrl: String? = null,
    @SerialName("image")
    val image: String? = null,
    val url: String? = null
) {
    fun resolveUrl(): String? = listOf(imageUrl, image, url)
        .firstOrNull { !it.isNullOrBlank() }
}
