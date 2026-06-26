package com.example.fooddelivery.data.remote.dto

import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

private object FlexibleDoubleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleDouble", PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeDouble(value)
    }

    override fun deserialize(decoder: Decoder): Double {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeDouble()
        val element = jsonDecoder.decodeJsonElement().jsonPrimitive
        return element.doubleOrNull ?: element.content.toDoubleOrNull() ?: 0.0
    }
}

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
    @Serializable(with = FlexibleDoubleSerializer::class)
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
@Serializable
data class VoucherListResponseDto(
    val success: Boolean? = null,
    val data: List<VoucherDto> = emptyList(),
    val total: Int? = 0,
    val limit: Int? = 0,
    val offset: Int? = 0
)
