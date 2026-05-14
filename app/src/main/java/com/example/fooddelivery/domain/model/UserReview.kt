package com.example.fooddelivery.domain.model

data class UserReview(
    val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val restaurantImage: String,
    val rating: Int,
    val comment: String,
    val tags: List<String>,
    val createdAt: Long,
    val orderId: String
) {
    init {
        require(rating in 0..5) { "Rating must be between 0 and 5, but was $rating" }
    }
    val canEditOrDelete: Boolean
        get() {
            val currentTime = System.currentTimeMillis()
            val oneWeekMillis = 7 * 24 * 60 * 60 * 1000L
            return currentTime - createdAt < oneWeekMillis
        }
}