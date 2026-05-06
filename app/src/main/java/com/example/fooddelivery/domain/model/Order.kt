package com.example.fooddelivery.domain.model

enum class OrderStatus {
    ONGOING,
    COMPLETED,
    CANCELED
}

enum class OrderType {
    FOOD,
    DRINK
}

data class Order(
    val id: String,
    val restaurantName: String,
    val restaurantImage: String,
    val price: Double,
    val itemCount: Int,
    val type: OrderType,
    val status: OrderStatus,
    val date: String? = null
)
