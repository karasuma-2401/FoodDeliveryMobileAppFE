package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val image: String,
    val description: String? = null,
    val sortOrder: Int? = null,
    val foodCount: Int? = null
)
