package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDetailDto(
    val conversation: ConversationDto,
    val messages: List<MessageDto>,
    val pagination: PaginationDto
)

@Serializable
data class PaginationDto(
    val total: Int,
    val limit: Int,
    val offset: Int
)
