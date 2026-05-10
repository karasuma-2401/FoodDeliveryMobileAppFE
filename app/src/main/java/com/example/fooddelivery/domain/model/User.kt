package com.example.fooddelivery.domain.model

data class User (
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val bio: String = "",
    val profileImage: String? = null
)