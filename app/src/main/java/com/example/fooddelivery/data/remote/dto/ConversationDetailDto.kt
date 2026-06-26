package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDetailDto(
    val conversation: ConversationDto,
    val messages: List<MessageDto>
)
