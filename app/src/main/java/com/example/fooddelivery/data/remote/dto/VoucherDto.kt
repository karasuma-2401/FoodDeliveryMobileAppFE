package com.example.fooddelivery.data.remote.dto

import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import kotlinx.serialization.Serializable

@Serializable
data class VoucherRestaurantDto(
    val id: Int,
    val name: String
)

@Serializable
data class VoucherDto(
    val id: Int,
    val name: String,
    val code: String,
    val description: String? = null,
    val image: String? = null,
    val sale: Double,
    val type: String,
    val status: String,
    val minimumOrderAmount: Double = 0.0,
    val maximumDiscountAmount: Double? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val restaurantId: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deleteAt: String? = null,
    val restaurant: VoucherRestaurantDto? = null
)

fun VoucherDto.toDomain(): Voucher {
    return Voucher(
        id = id,
        code = code,
        title = name,
        description = description ?: "",
        image = image,
        discountAmount = sale,
        minOrderAmount = minimumOrderAmount,
        maxDiscountAmount = maximumDiscountAmount,
        expiryText = endAt,
        startAt = startAt,
        type = if (type == "PERCENT") VoucherType.PERCENT else VoucherType.MONEY,
        isApplicable = true,
        conditionMessage = null,
        restaurantName = restaurant?.name
    )
}
