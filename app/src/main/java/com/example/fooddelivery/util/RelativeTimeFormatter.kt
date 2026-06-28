package com.example.fooddelivery.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatRelativeTimeEnglish(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val diffMillis = (now - timestamp).coerceAtLeast(0)
    return when {
        diffMillis < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diffMillis < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis).toInt().coerceAtLeast(1)
            if (minutes == 1) "1 min ago" else "$minutes min ago"
        }
        diffMillis < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diffMillis).toInt().coerceAtLeast(1)
            if (hours == 1) "1 hr ago" else "$hours hr ago"
        }
        diffMillis < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
        diffMillis < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt().coerceAtLeast(2)
            if (days == 1) "1 day ago" else "$days days ago"
        }
        else -> SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(timestamp))
    }
}
