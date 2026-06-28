package com.example.fooddelivery.data.remote.dto

import com.example.fooddelivery.domain.model.Address
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserAddressRequest(
    val title: String? = null,
    val addressDetail: String? = null,
    val address: AddressMapBundleDto? = null,
)

@Serializable
data class AddressMapBundleDto(
    val title: String? = null,
    val latitude: Double,
    val longitude: Double,
    val fullText: String,
)

@Serializable
data class UpdateUserAddressRequest(
    val title: String? = null,
    val addressDetail: String? = null,
)

@Serializable
data class UpdateUserAddressLocationRequest(
    val title: String,
    val fullText: String,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class AddressDetailDto(
    val id: Int? = null,
    val title: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val fullText: String? = null,
    val updatedAt: String? = null,
    val deleteAt: String? = null,
)

@Serializable
data class AddressResponse(
    val id: Int,
    val title: String,
    val addressDetail: String? = null,
    val address: AddressDetailDto,
    val deleteAt: String? = null,
)

@Serializable
data class DeleteAddressResponse(
    val message: String,
    val id: Int,
)

fun Address.toCreateUserAddressRequest(): CreateUserAddressRequest {
    return CreateUserAddressRequest(
        title = type,
        addressDetail = deliveryNote.takeIf { it.isNotBlank() },
        address = AddressMapBundleDto(
            title = photonPlaceTitle(),
            latitude = latitude,
            longitude = longitude,
            fullText = detail,
        ),
    )
}

fun Address.toUpdateUserAddressRequest(): UpdateUserAddressRequest {
    return UpdateUserAddressRequest(
        title = type,
        addressDetail = deliveryNote.takeIf { it.isNotBlank() },
    )
}

fun Address.toUpdateUserAddressLocationRequest(): UpdateUserAddressLocationRequest {
    return UpdateUserAddressLocationRequest(
        title = photonPlaceTitle(),
        fullText = detail,
        latitude = latitude,
        longitude = longitude,
    )
}

/** Photon/map place name for [Address.title] — not [Address.type] (user label). */
fun Address.photonPlaceTitle(): String =
    title.ifBlank { detail.substringBefore(',').trim().ifBlank { detail } }

fun AddressResponse.toAddress(): Address {
    return Address(
        id = id,
        type = title,
        title = address.title ?: "",
        detail = address.fullText ?: "",
        deliveryNote = addressDetail.orEmpty(),
        latitude = address.latitude ?: 0.0,
        longitude = address.longitude ?: 0.0,
    )
}

fun AddressResponse.toRestaurantAddress(): Address {
    return Address(
        id = address.id ?: 0,
        type = title,
        title = address.title ?: "",
        detail = address.fullText ?: "",
        deliveryNote = addressDetail.orEmpty(),
        latitude = address.latitude ?: 0.0,
        longitude = address.longitude ?: 0.0,
    )
}
