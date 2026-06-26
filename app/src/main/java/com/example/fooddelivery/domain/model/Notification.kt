package com.example.fooddelivery.domain.model

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType,
    val isRead: Boolean,
    val targetId: String?,
    val targetType: String? = null,
    val actions: List<String> = emptyList()
)

fun Notification.effectiveActions(): List<String> {
    if (actions.isNotEmpty()) return actions
    if (targetType.equals("RESTAURANT", ignoreCase = true) && type == NotificationType.SYSTEM) {
        return listOf("APPROVE_VENDOR", "REJECT_VENDOR")
    }
    return emptyList()
}

enum class NotificationType {
    ORDER,
    PROMOTION,
    SYSTEM,
    PAYMENT,
    CHAT
}