package com.example.fooddelivery.data.local.room.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fooddelivery.domain.model.NotificationType
import com.example.fooddelivery.domain.model.Notification


@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String,
    val isRead: Boolean,
    val targetId: String?,
    val targetType: String? = null,
    val actions: String = ""
)
// mapper function
fun NotificationEntity.toDomain(): Notification {
    return Notification(
        id = id,
        title = title,
        message = message,
        timestamp = timestamp,
        type = NotificationType.valueOf(type),
        isRead = isRead,
        targetId = targetId,
        targetType = targetType,
        actions = actions.split(",").filter { it.isNotBlank() }
    )
}
fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        title = title,
        message = message,
        timestamp = timestamp,
        type = type.name,
        isRead = isRead,
        targetId = targetId,
        targetType = targetType,
        actions = actions.joinToString(",")
    )
}