package com.example.fooddelivery.domain.model

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType,
    val isRead: Boolean,
    val targetId: String?
)
enum class NotificationType {
    ORDER,
    PROMOTION,
    SYSTEM
}