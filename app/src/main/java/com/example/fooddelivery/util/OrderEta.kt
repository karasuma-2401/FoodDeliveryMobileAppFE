package com.example.fooddelivery.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale
import java.util.TimeZone

object OrderEta {

    fun parseIsoMillis(iso: String): Long? {
        if (iso.isBlank() || !iso.contains('T')) return null
        return try {
            Instant.parse(normalizeIsoInstant(iso)).toEpochMilli()
        } catch (_: Exception) {
            try {
                val clean = iso.substringBefore(".").substringBefore("Z")
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                isoFormat.parse(clean)?.time
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Returns a user-facing countdown label for [expectedArrivalIso], or null when unparseable.
     */
    fun countdownLabel(expectedArrivalIso: String, nowMillis: Long = System.currentTimeMillis()): String? {
        val arrivalMillis = parseIsoMillis(expectedArrivalIso) ?: return null
        val diffMs = arrivalMillis - nowMillis
        return when {
            diffMs <= 0 -> "Taking a bit longer than expected"
            diffMs < 60_000 -> "Arriving soon"
            else -> {
                val diffMinutes = (diffMs / 60_000).toInt()
                when {
                    diffMinutes < 60 -> "~$diffMinutes min left"
                    else -> {
                        val hours = diffMinutes / 60
                        val mins = diffMinutes % 60
                        if (mins == 0) "~$hours hr left" else "~${hours} hr $mins min left"
                    }
                }
            }
        }
    }

    private fun normalizeIsoInstant(iso: String): String {
        val trimmed = iso.trim()
        return when {
            trimmed.endsWith('Z') || trimmed.contains('+') -> trimmed
            else -> "${trimmed}Z"
        }
    }
}
