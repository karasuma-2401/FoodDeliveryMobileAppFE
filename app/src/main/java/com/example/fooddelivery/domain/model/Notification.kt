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

sealed interface NotificationDestination {
    data class Order(val orderId: String) : NotificationDestination
    data class Chat(val conversationId: String) : NotificationDestination
    data class RestaurantApproval(val restaurantId: String) : NotificationDestination
}

fun Notification.resolveDestination(): NotificationDestination? {
    val target = targetId ?: return null
    return when {
        type == NotificationType.CHAT -> NotificationDestination.Chat(target)
        type == NotificationType.ORDER -> NotificationDestination.Order(target)
        type == NotificationType.PAYMENT -> NotificationDestination.Order(target)
        type == NotificationType.SYSTEM &&
            targetType.equals("RESTAURANT", ignoreCase = true) ->
            NotificationDestination.RestaurantApproval(target)
        else -> null
    }
}

enum class NotificationType {
    ORDER,
    PROMOTION,
    SYSTEM,
    PAYMENT,
    CHAT
}
