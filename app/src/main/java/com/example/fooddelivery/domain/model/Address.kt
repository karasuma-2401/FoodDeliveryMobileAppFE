package com.example.fooddelivery.domain.model

data class Address(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val streetName: String = "",
    val city: String = "",
    val detail: String = "",
    val isDefault: Boolean = false
)
