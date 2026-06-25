package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: Int,
    val orderId: Int,
    val customerId: Int,
    val sellerId: Int,
    val createdAt: String,
    val updatedAt: String? = null,
    val lastMessage: LastMessageDto? = null,
    val unreadCount: Int = 0,
    val customer: OtherUserDto? = null,
    val seller: OtherUserDto? = null,
    val restaurant: ConversationRestaurantDto? = null,
    val other: OtherUserDto? = null
)

@Serializable
data class ConversationRestaurantDto(
    val id: Int,
    val name: String,
    val image: String = ""
)

@Serializable
data class LastMessageDto(
    val content: String,
    val createdAt: String
)

@Serializable
data class OtherUserDto(
    val id: Int,
    val name: String,
    val avatar: String? = null
)
