package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: Int,
    val conversationId: Int,
    val senderId: Int,
    val content: String,
    @SerialName("image")
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val createdAt: String,
    val who: String? = null
)
