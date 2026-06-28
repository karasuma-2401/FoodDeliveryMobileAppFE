package com.example.fooddelivery.util

import com.example.fooddelivery.domain.model.Address

fun Address.formatLocationLine(): String {
    val parts = listOfNotNull(
        streetName.takeIf { it.isNotBlank() },
        city.takeIf { it.isNotBlank() }
    )
    return parts.joinToString(", ").ifBlank { detail.ifBlank { title } }
}

fun Address.hasValidCoordinates(): Boolean =
    latitude != 0.0 && longitude != 0.0
