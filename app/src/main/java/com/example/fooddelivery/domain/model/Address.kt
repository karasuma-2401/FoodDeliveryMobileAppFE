package com.example.fooddelivery.domain.model

data class Address(
    val id: Int = 0,
    val type: String = "", // Label (e.g., "Home", "Work")
    val title: String = "", // Physical address title
    val streetName: String = "",
    val city: String = "",
    val detail: String = "", // Full address text from map/search
    val deliveryNote: String = "", // Free-form note (floor, gate, call before arrival)
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
