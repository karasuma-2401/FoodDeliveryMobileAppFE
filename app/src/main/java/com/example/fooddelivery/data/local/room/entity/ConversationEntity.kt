package com.example.fooddelivery.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val restaurantName: String,
    val restaurantImage: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int
)