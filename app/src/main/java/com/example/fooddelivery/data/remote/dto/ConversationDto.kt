package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: String,
    val restaurantName: String,
    val restaurantImage: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int
)
