package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: Int,
    val orderId: Int,
    val customerId: Int,
    val sellerId: Int,
    val createdAt: String,
    val lastMessage: LastMessageDto? = null,
    val order: OrderInfoDto? = null,
    // Các trường BE sẽ bổ sung sau để phục vụ UI
    val sellerName: String? = null,
    val sellerImage: String? = null,
    val unreadCount: Int? = 0
)

@Serializable
data class LastMessageDto(
    val content: String,
    val createdAt: String
)

@Serializable
data class OrderInfoDto(
    val id: Int,
    val status: String
)
