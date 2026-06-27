package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.Address
import kotlinx.coroutines.flow.StateFlow

data class DeliveryAddressOption(
    val id: Int,
    val label: String,
    val fullText: String,
)

data class DeliveryLocationState(
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: Int? = null,
) {
    val hasAddresses: Boolean
        get() = addresses.isNotEmpty()

    val selectedAddress: Address?
        get() = addresses.find { it.id == selectedAddressId } ?: addresses.firstOrNull()

    val selectedAddressLabel: String
        get() = selectedAddress?.toAddressLabel().orEmpty()

    val selectedAddressDetail: String
        get() {
            val address = selectedAddress
            return if (address != null) {
                address.toAddressDetail()
            } else {
                EMPTY_PLACEHOLDER
            }
        }

    /** @deprecated Use [selectedAddressLabel] — kept for legacy call sites */
    val selectedLabel: String
        get() = selectedAddressLabel.ifEmpty { selectedAddressDetail }

    val availableAddressOptions: List<DeliveryAddressOption>
        get() = addresses.map { it.toDeliveryAddressOption() }

    /** @deprecated Use [availableAddressOptions] */
    val availableLabels: List<String>
        get() = availableAddressOptions.map { it.label }

    val lat: Double?
        get() = selectedAddress?.latitude?.takeIf { it != 0.0 }

    val lng: Double?
        get() = selectedAddress?.longitude?.takeIf { it != 0.0 }

    companion object {
        const val EMPTY_PLACEHOLDER = "Chọn địa chỉ giao hàng"
    }
}

fun Address.toAddressLabel(): String =
    type.ifEmpty { title.ifEmpty { "Address" } }

fun Address.toAddressDetail(): String =
    detail.ifEmpty { title.ifEmpty { type } }

fun Address.toDeliveryAddressOption(): DeliveryAddressOption =
    DeliveryAddressOption(
        id = id,
        label = toAddressLabel(),
        fullText = toAddressDetail(),
    )

/** @deprecated Use [toAddressLabel] */
fun Address.toDisplayLabel(): String = toAddressLabel()

interface DeliveryLocationRepository {
    val deliveryLocation: StateFlow<DeliveryLocationState>

    suspend fun refreshAddresses()

    suspend fun selectByLabel(label: String)

    suspend fun selectById(addressId: Int)
}
