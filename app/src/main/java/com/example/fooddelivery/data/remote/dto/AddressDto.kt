package com.example.fooddelivery.data.remote.dto

import com.example.fooddelivery.domain.model.Address
import kotlinx.serialization.Serializable

@Serializable
data class AddressRequest(
    val title: String? = null, // Label like "Home", "Work"
    val address: AddressDetailDto? = null
)

@Serializable
data class AddressDetailDto(
    val id: Int? = null,
    val title: String? = null, // Physical address title
    val latitude: Double? = null,
    val longitude: Double? = null,
    val fullText: String? = null,
    val updatedAt: String? = null,
    val deleteAt: String? = null
)

@Serializable
data class AddressResponse(
    val id: Int,
    val title: String, // Label like "Home", "Work"
    val address: AddressDetailDto
)

@Serializable
data class DeleteAddressResponse(
    val message: String,
    val id: Int
)

fun Address.toAddressRequest(): AddressRequest {
    return AddressRequest(
        title = type,
        address = AddressDetailDto(
            title = title,
            latitude = latitude,
            longitude = longitude,
            fullText = detail
        )
    )
}

fun AddressResponse.toAddress(): Address {
    return Address(
        id = id,
        type = title,
        title = address.title ?: "",
        detail = address.fullText ?: "",
        latitude = address.latitude ?: 0.0,
        longitude = address.longitude ?: 0.0
    )
}
