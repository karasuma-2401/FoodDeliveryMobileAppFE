package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequest(
    val sellerId: Int
)
