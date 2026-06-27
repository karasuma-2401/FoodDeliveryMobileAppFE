package com.example.fooddelivery.domain.model

data class ReviewItem(
    val id: String,
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val date: String,
    val title: String,
    val rating: Int,
    val description: String,
    val reply: String? = null,
    val userAvatarRes: Int? = null
) {
    init {
        require(rating in 1..5) { "Rating must be between 1 and 5" }
    }
}
