package com.example.fooddelivery.util

import com.example.fooddelivery.data.local.room.entity.MessageEntity
import java.time.Instant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun messageCreatedAtMillis(createdAt: String): Long {
    if (createdAt.isBlank()) return 0L
    createdAt.toLongOrNull()?.let { return it }
    if (!createdAt.contains('T')) return 0L
    return try {
        Instant.parse(normalizeIsoInstant(createdAt)).toEpochMilli()
    } catch (_: Exception) {
        try {
            val clean = createdAt.substringBefore(".").substringBefore("Z")
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            isoFormat.parse(clean)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }
}

private fun normalizeIsoInstant(createdAt: String): String {
    val trimmed = createdAt.trim()
    return when {
        trimmed.endsWith('Z') || trimmed.contains('+') -> trimmed
        else -> "${trimmed}Z"
    }
}

fun messageServerIdSortKey(id: String): Long = id.toLongOrNull() ?: Long.MAX_VALUE

fun messageTimelineComparator(): Comparator<MessageEntity> =
    compareBy({ messageCreatedAtMillis(it.createdAt) }, { messageServerIdSortKey(it.id) })

fun normalizeCreatedAt(createdAt: String): String =
    messageCreatedAtMillis(createdAt).toString()

fun normalizeCreatedAtNow(): String = System.currentTimeMillis().toString()

/** Formats a server-normalized or ISO timestamp for display in the device timezone. */
fun formatMessageDisplayTime(createdAt: String): String {
    val millis = messageCreatedAtMillis(createdAt)
    if (millis <= 0L) return ""
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))
}

fun messagesForMessengerDisplay(messages: List<MessageEntity>): List<MessageEntity> =
    messages.sortedWith(messageTimelineComparator()).asReversed()

fun conversationPreviewFromMessage(content: String, imageUrl: String?): String? = when {
    content.isNotBlank() -> content
    !imageUrl.isNullOrBlank() -> "Photo"
    else -> null
}

fun senderIdsMatch(left: String, right: String): Boolean {
    if (left.isBlank() || right.isBlank()) return false
    if (left == right) return true
    val leftInt = left.toIntOrNull()
    val rightInt = right.toIntOrNull()
    return leftInt != null && rightInt != null && leftInt == rightInt
}
