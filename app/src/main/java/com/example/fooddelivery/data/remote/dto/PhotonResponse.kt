package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PhotonResponse(
    val features: List<PhotonFeature>
)

@Serializable
data class PhotonFeature(
    val properties: PhotonProperties,
    val geometry: PhotonGeometry
)

@Serializable
data class PhotonProperties(
    val name: String? = null,
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postcode: String? = null,
    val houseNumber: String? = null
) {
    fun getDisplayName(): String {
        return listOfNotNull(name, street, city, country).joinToString(", ")
    }
}
@Serializable
data class PhotonGeometry(
    val coordinates: List<Double>
)