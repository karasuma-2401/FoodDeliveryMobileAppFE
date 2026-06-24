package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.AddressApi
import com.example.fooddelivery.data.remote.api.PhotonService
import com.example.fooddelivery.data.remote.dto.toAddress
import com.example.fooddelivery.data.remote.dto.toAddressRequest
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.repository.AddressRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AddressRepositoryImpl @Inject constructor(
    private val addressApi: AddressApi,
    private val photonService: PhotonService
) : AddressRepository {

    override suspend fun getAddresses(): Result<List<Address>> {
        return try {
            val response = addressApi.getAddresses()
            if (response.isSuccessful && response.body() != null) {
                val activeAddresses = response.body()!!
                    .filter { it.address.deleteAt == null }
                    .map { it.toAddress() }
                Result.success(activeAddresses)
            } else {
                Result.failure(Exception("Failed to get addresses: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getAddressById(addressId: Int): Result<Address> {
        return try {
            val response = addressApi.getAddress(addressId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toAddress())
            } else {
                Result.failure(Exception("Failed to get address: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun addAddress(address: Address): Result<Unit> {
        return try {
            val response = addressApi.addAddress(address.toAddressRequest())
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add address: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun updateAddress(address: Address): Result<Unit> {
        return try {
            val response = addressApi.updateAddress(address.id, address.toAddressRequest())
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update address: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun deleteAddress(addressId: Int): Result<Unit> {
        return try {
            val response = addressApi.deleteAddress(addressId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete address: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun searchPlaces(query: String): Result<List<Address>> {
        return try {
            val response = photonService.search(query)
            val addresses = response.features.map { feature ->
                Address(
                    title = feature.properties.name ?: "",
                    streetName = feature.properties.street ?: feature.properties.name ?: "",
                    city = feature.properties.city ?: "",
                    detail = feature.properties.getDisplayName(),
                    latitude = feature.geometry.coordinates[1],
                    longitude = feature.geometry.coordinates[0]
                )
            }
            Result.success(addresses)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}
