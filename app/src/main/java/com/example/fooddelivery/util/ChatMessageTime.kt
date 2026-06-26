package com.example.fooddelivery.util

import com.example.fooddelivery.data.local.room.entity.MessageEntity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun messageCreatedAtMillis(createdAt: String): Long {
    if (createdAt.isBlank()) return 0L
    createdAt.toLongOrNull()?.let { return it }
    return try {
        val clean = createdAt.substringBefore(".").substringBefore("Z")
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        isoFormat.parse(clean)?.time ?: 0L
    } catch (_: Exception) {
        0L
    }
}

fun normalizeCreatedAt(createdAt: String): String =
    messageCreatedAtMillis(createdAt).toString()

fun normalizeCreatedAtNow(): String = System.currentTimeMillis().toString()

fun messagesForMessengerDisplay(messages: List<MessageEntity>): List<MessageEntity> =
    messages.sortedBy { messageCreatedAtMillis(it.createdAt) }.asReversed()

fun senderIdsMatch(left: String, right: String): Boolean {
    if (left.isBlank() || right.isBlank()) return false
    if (left == right) return true
    val leftInt = left.toIntOrNull()
    val rightInt = right.toIntOrNull()
    return leftInt != null && rightInt != null && leftInt == rightInt
}
