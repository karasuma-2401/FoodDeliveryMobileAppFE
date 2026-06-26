package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.Address
import kotlinx.coroutines.flow.StateFlow

data class DeliveryLocationState(
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: Int? = null,
) {
    val selectedLabel: String
        get() = selectedAddress?.toDisplayLabel() ?: DEFAULT_LABEL

    val availableLabels: List<String>
        get() = addresses.map { it.toDisplayLabel() }.ifEmpty { DEFAULT_LABELS }

    val selectedAddress: Address?
        get() = addresses.find { it.id == selectedAddressId } ?: addresses.firstOrNull()

    val lat: Double?
        get() = selectedAddress?.latitude?.takeIf { it != 0.0 }

    val lng: Double?
        get() = selectedAddress?.longitude?.takeIf { it != 0.0 }

    companion object {
        const val DEFAULT_LABEL = "Home"
        val DEFAULT_LABELS = listOf("Home", "Work", "Other")
    }
}

fun Address.toDisplayLabel(): String = type.ifEmpty { title.ifEmpty { detail } }

interface DeliveryLocationRepository {
    val deliveryLocation: StateFlow<DeliveryLocationState>

    suspend fun refreshAddresses()

    suspend fun selectByLabel(label: String)

    suspend fun selectById(addressId: Int)
}
