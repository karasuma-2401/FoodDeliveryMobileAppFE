package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderRequest(
    val restaurantId: Int,
    val voucherId: Int? = null,
    val savedAddressId: Int? = null,
    val customAddress: CustomAddressRequest? = null,
    val orderFoods: List<OrderItemRequest>,
    val note: String? = null,
    val paymentMethod: String,
    val clearCartAfterOrder: Boolean = true,
    val totalAmount: Double? = null
)

@Serializable
data class CustomAddressRequest(
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val fullText: String
)

@Serializable
data class OrderItemRequest(
    val foodId: Int,
    val quantity: Int,
    val fullText: String? = null,
    val foodSizeId: Int? = null
)

@Serializable
data class OrderResponse(
    val id: Int,
    val status: String,
    val orderCode: String? = null,
    val totalAmount: Double? = null,
    val createdAt: String? = null,
    val deeplink: String? = null
)

@Serializable
data class OrderStatusSummaryResponse(
    val order_id: Int,
    val status: String,
    val status_step: Int,
    val updated_at: String,
    val backend_status: String
)

@Serializable
data class CancelOrderResponse(
    val order_id: Int,
    val new_status: String,
    val status: String,
    val status_step: Int,
    val message: String
)

@Serializable
data class UpdateOrderStatusRequest(
    val status: String
)

// DTOs for Order List and Details
@Serializable
data class OrderListDto(
    val id: Int,
    val totalPrice: Double,
    val status: String,
    val status_step: Int? = null,
    val backend_status: String? = null,
    val item_count: Int,
    val type: String,
    val date: String,
    val address: OrderAddressDto? = null,
    val restaurant: OrderRestaurantBriefDto? = null,
    val orderFoods: List<OrderFoodBriefDto> = emptyList(),
    val voucher: OrderVoucherDto? = null,
    val payment: OrderPaymentDto? = null
)

@Serializable
data class OrderDetailResponse(
    val id: Int,
    val totalPrice: Double,
    val status: String,
    val status_step: Int? = null,
    val backend_status: String? = null,
    val expected_arrival: String? = null,
    val user: OrderUserDto? = null,
    val address: OrderAddressDto? = null,
    val restaurant: OrderRestaurantBriefDto? = null,
    val orderFoods: List<OrderFoodBriefDto> = emptyList(),
    val voucher: OrderVoucherDto? = null,
    val note: String? = null,
    val payment: OrderPaymentDto? = null,
    val conversation: OrderConversationDto? = null
)

@Serializable
data class OrderUserDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null
)

@Serializable
data class OrderConversationDto(
    val id: Int,
    val participantIds: List<Int> = emptyList()
)

@Serializable
data class OrderAddressDto(
    val id: Int,
    val title: String,
    val fullText: String
)

@Serializable
data class OrderRestaurantBriefDto(
    val id: Int,
    val name: String,
    val image: String
)

@Serializable
data class OrderFoodBriefDto(
    val id: Int,
    val name: String,
    val image: String,
    val quantity: Int,
    val price: Double,
    val foodSizeId: Int? = null,
    val sizeName: String? = null
)

@Serializable
data class OrderVoucherDto(
    val id: Int,
    val name: String
)

@Serializable
data class OrderPaymentDto(
    val id: Int,
    val paymentStatus: String,
    val method: String,
    val amount: Double,
    val createdAt: String
)

@Serializable
data class OngoingOrdersResponse(
    val ongoing_orders: List<OrderListDto>
)

@Serializable
data class HistoryOrdersResponse(
    val history_orders: List<OrderListDto>
)
