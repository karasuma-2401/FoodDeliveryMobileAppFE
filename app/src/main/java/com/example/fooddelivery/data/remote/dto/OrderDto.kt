package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable
import com.example.fooddelivery.data.remote.dto.MessageResponse
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
    val order: OrderDetailDto,
    val payment: PaymentDto,
    val momoPayment: MomoPaymentDto? = null,
    val conversation: OrderConversationDto? = null
)

@Serializable
data class OrderDetailDto(
    val id: Int,
    val restaurantId: Int,
    val totalPrice: Double,
    val status: String,
    val userId: Int,
    val addressId: Int,
    val voucherId: Int? = null,
    val note: String? = null,
    val createdAt: String? = null
)

@Serializable
data class PaymentDto(
    val id: Int,
    val orderId: Int,
    val amount: Double,
    val method: String,
    val paymentStatus: String,
    val createdAt: String? = null
)

@Serializable
data class MomoPaymentDto(
    val partnerCode: String,
    val orderId: String,
    val requestId: String,
    val payUrl: String,
    val deeplink: String,
    val qrCodeUrl: String,
    val resultCode: Int,
    val message: String
)

@Serializable
data class OrderConversationDto(
    val id: Int,
    val orderId: Int,
    val customerId: Int,
    val sellerId: Int,
    val updatedAt: String? = null
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
    val note: String? = null,
    val expected_arrival: String? = null,
    val user: OrderUserDto? = null,
    val address: OrderAddressDto? = null,
    val restaurant: OrderRestaurantBriefDto? = null,
    val orderFoods: List<OrderFoodBriefDto> = emptyList(),
    val voucher: OrderVoucherDto? = null,
    val payment: OrderPaymentDto? = null,
    val conversation: OrderConversationDto? = null
)

@Serializable
data class OrderUserDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String
)

@Serializable
data class OrderAddressDto(
    val id: Int,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val fullText: String
)

@Serializable
data class OrderRestaurantBriefDto(
    val id: Int,
    val name: String,
    val image: String,
    val phone: String? = null,
    val ownerId: Int? = null,
    val estimatedDeliveryTime: Int? = null
)

@Serializable
data class OrderFoodBriefDto(
    val id: Int,
    val quantity: Int,
    val fullText: String? = null,
    val price: Double,
    val foodSizeId: Int? = null,
    val sizeName: String? = null,
    val food: OrderFoodDetailDto? = null
)

@Serializable
data class OrderFoodDetailDto(
    val id: Int,
    val name: String,
    val image: String,
    val description: String? = null,
    val label: String? = null
)

@Serializable
data class OrderVoucherDto(
    val id: Int,
    val name: String,
    val sale: Double? = null,
    val type: String? = null
)

@Serializable
data class OrderPaymentDto(
    val id: Int,
    val amount: Double,
    val method: String,
    val paymentStatus: String,
    val createdAt: String? = null
)

@Serializable
data class OngoingOrdersResponse(
    val ongoing_orders: List<OrderListDto>
)

@Serializable
data class HistoryOrdersResponse(
    val history_orders: List<OrderListDto>
)
