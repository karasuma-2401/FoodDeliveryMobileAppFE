package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import com.example.fooddelivery.data.remote.dto.MessageResponse

private object FlexibleStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeString()
        return jsonDecoder.decodeJsonElement().jsonPrimitive.content
    }
}

@Serializable
data class OrderRequest(
    val restaurantId: Int,
    val voucherId: Int? = null,
    val savedAddressId: Int? = null,
    val customAddress: CustomAddressRequest? = null,
    val orderFoods: List<OrderItemRequest>,
    val note: String? = null,
    val paymentMethod: String,
    val clearCartAfterOrder: Boolean = false,
    val totalAmount: Double? = null
)

@Serializable
data class DeliveryFeeResponse(
    val restaurantId: Int,
    val deliveryFee: Double
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
    @SerialName("paymentInformation")
    val paymentInformation: PaymentInformationDto,
    val conversation: OrderConversationDto? = null,
    val items: List<CreateOrderItemDto> = emptyList(),
)

@Serializable
data class CreateOrderItemDto(
    val orderId: Int,
    val foodId: Int,
    val foodSizeId: Int? = null,
    val sizeName: String? = null,
    val quantity: Int,
    val fullText: String? = null,
    val price: Double,
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
    val deliveryFee: Double? = null,
    val createdAt: String? = null
)

@Serializable
data class PaymentInformationDto(
    val id: Int? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val orderId: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val amount: String? = null,
    val method: String? = null,
    val paymentStatus: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deleteAt: String? = null,
    val partnerCode: String? = null,
    val requestId: String? = null,
    val payUrl: String? = null,
    val deeplink: String? = null,
    val qrCodeUrl: String? = null,
    val resultCode: Int? = null,
    val message: String? = null
)

@Serializable
data class OrderConversationDto(
    val id: Int,
    val orderId: Int,
    val customerId: Int,
    val sellerId: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deleteAt: String? = null
)

@Serializable
data class OrderStatusSummaryResponse(
    val order_id: Int,
    val status: String,
    val status_step: Int,
    val updated_at: String,
    val backend_status: String,
    val delivered_at: String? = null,
    val auto_confirm_at: String? = null,
    val hours_until_auto_confirm: Double? = null,
    val confirmed_at: String? = null,
    val confirmed_by: String? = null
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
    val item_count: Int = 0,
    val type: String = "FOOD",
    val date: String = "",
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
    val delivered_at: String? = null,
    val auto_confirm_at: String? = null,
    val hours_until_auto_confirm: Double? = null,
    val confirmed_at: String? = null,
    val confirmed_by: String? = null,
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
    val id: Int? = null,
    val title: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val fullText: String? = null
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
    val name: String? = null,
    val image: String? = null,
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

@Serializable
data class ReorderResponse(
    val cart: CartResponse,
    val addedCount: Int,
    val skippedItems: List<SkippedReorderItemResponse> = emptyList(),
    val message: String? = null,
)

@Serializable
data class SkippedReorderItemResponse(
    val foodId: Int,
    val reason: String,
)
