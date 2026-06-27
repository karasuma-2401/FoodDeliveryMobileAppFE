package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.local.datastore.DataStoreManager
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.repository.AddressRepository
import com.example.fooddelivery.domain.repository.DeliveryLocationRepository
import com.example.fooddelivery.domain.repository.DeliveryLocationState
import com.example.fooddelivery.domain.repository.toAddressLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryLocationRepositoryImpl @Inject constructor(
    private val addressRepository: AddressRepository,
    private val dataStoreManager: DataStoreManager,
) : DeliveryLocationRepository {

    private val _deliveryLocation = MutableStateFlow(DeliveryLocationState())
    override val deliveryLocation: StateFlow<DeliveryLocationState> = _deliveryLocation.asStateFlow()

    override suspend fun refreshAddresses() {
        addressRepository.getAddresses().onSuccess { addresses ->
            val persistedId = dataStoreManager.readSelectedAddressId().first()
            val selectedId = resolveSelectedAddressId(addresses, persistedId)
            if (selectedId != null && selectedId != persistedId) {
                dataStoreManager.saveSelectedAddressId(selectedId)
            }
            _deliveryLocation.update {
                DeliveryLocationState(
                    addresses = addresses,
                    selectedAddressId = selectedId,
                )
            }
        }
    }

    override suspend fun selectByLabel(label: String) {
        val addresses = _deliveryLocation.value.addresses
        val selected = addresses.find { it.toAddressLabel() == label } ?: return
        selectById(selected.id)
    }

    override suspend fun selectById(addressId: Int) {
        val addresses = _deliveryLocation.value.addresses
        if (addresses.none { it.id == addressId }) return
        dataStoreManager.saveSelectedAddressId(addressId)
        _deliveryLocation.update { it.copy(selectedAddressId = addressId) }
    }

    private fun resolveSelectedAddressId(
        addresses: List<Address>,
        persistedId: Int?,
    ): Int? {
        if (addresses.isEmpty()) return null
        if (persistedId != null && addresses.any { it.id == persistedId }) {
            return persistedId
        }
        val currentId = _deliveryLocation.value.selectedAddressId
        if (currentId != null && addresses.any { it.id == currentId }) {
            return currentId
        }
        return addresses.first().id
    }
}
