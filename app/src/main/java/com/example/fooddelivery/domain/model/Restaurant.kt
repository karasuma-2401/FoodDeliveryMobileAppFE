package com.example.fooddelivery.domain.model

data class Restaurant(
    val id: String,
    val name: String,
    val description: String = "",
    val tags: List<String>,
    val rating: Float,
    val reviewCount: Int = 0,
    val deliveryFee: Double,
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val promoTags: List<String> = emptyList(),
    val isLiked: Boolean = false,
    val totalLikes: Int = 0,
    val distance: Double? = null,
    val estimatedDeliveryTime: Int? = null,
    val hasVoucher: Boolean = false,
    val voucherBadgeLabel: String? = null
)
