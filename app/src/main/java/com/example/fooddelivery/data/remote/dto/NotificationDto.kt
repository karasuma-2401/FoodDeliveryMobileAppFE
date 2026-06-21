package com.example.fooddelivery.data.remote.dto

import com.example.fooddelivery.domain.model.Notification
import com.example.fooddelivery.domain.model.NotificationType
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Serializable
data class NotificationResponse(
    val data: List<NotificationDto>
)

@Serializable
data class UnreadCountResponse(
    val count: Int
)

@Serializable
data class NotificationDto(
    val id: Int,
    val title: String,
    val body: String,
    val type: String,
    val userId: Int,
    val createdAt: String,
    val readAt: String? = null,
    val deleteAt: String? = null,
    val targetType: String? = null,
    val targetId: Int? = null,
    val actorId: Int? = null,
    val metadata: Map<String, String>? = null,
    val channels: List<NotificationChannelDto>? = null
)

@Serializable
data class NotificationChannelDto(
    val id: Int,
    val channel: String,
    val status: String,
    val sentAt: String? = null,
    val failedAt: String? = null,
    val error: String? = null,
    val providerResult: String? = null
)

@Serializable
data class CreateNotificationRequest(
    val title: String,
    val body: String,
    val type: String = "SYSTEM"
)

fun NotificationDto.toDomain(): Notification {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val timestamp = try {
        sdf.parse(createdAt)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    return Notification(
        id = id.toString(),
        title = title,
        message = body,
        timestamp = timestamp,
        type = try {
            NotificationType.valueOf(type.uppercase())
        } catch (e: Exception) {
            NotificationType.SYSTEM
        },
        isRead = readAt != null,
        targetId = targetId?.toString()
    )
}
