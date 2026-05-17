package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val imageUrl: String?,
    val createdAt: String
)
