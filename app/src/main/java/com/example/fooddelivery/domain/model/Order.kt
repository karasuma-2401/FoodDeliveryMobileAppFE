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
    val restaurantId: String,
    val restaurantName: String,
    val restaurantImage: String,
    val price: Double,
    val itemCount: Int,
    val type: OrderType,
    val status: OrderStatus,
    val date: String? = null
)

data class OrderDetail(
    val id: String,
    val totalPrice: Double,
    val status: String,
    val statusStep: Int,
    val backendStatus: String,
    val expectedArrival: String?,
    val restaurantId: Int,
    val restaurantName: String,
    val restaurantImage: String,
    val items: List<OrderItemDetail>,
    val address: String,
    val note: String?,
    val paymentMethod: String,
    val paymentStatus: String,
    val customerName: String,
    val customerPhone: String?,
    val conversationId: Int?
)

data class OrderItemDetail(
    val id: Int,
    val name: String,
    val image: String,
    val quantity: Int,
    val price: Double,
    val size: String?
)

data class OrderStatusSummary(
    val orderId: Int,
    val status: String,
    val statusStep: Int,
    val updatedAt: String,
    val backendStatus: String
)
