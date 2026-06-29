package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.AddressApi
import com.example.fooddelivery.data.remote.api.PhotonService
import com.example.fooddelivery.data.remote.dto.toAddress
import com.example.fooddelivery.data.remote.dto.toCreateUserAddressRequest
import com.example.fooddelivery.data.remote.dto.toRestaurantAddress
import com.example.fooddelivery.data.remote.dto.toUpdateUserAddressLocationRequest
import com.example.fooddelivery.data.remote.dto.toUpdateUserAddressRequest
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.data.remote.unwrapSuccess
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.repository.AddressRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AddressRepositoryImpl @Inject constructor(
    private val addressApi: AddressApi,
    private val photonService: PhotonService,
) : AddressRepository {

    override suspend fun getAddresses(): Result<List<Address>> {
        return try {
            addressApi.getAddresses()
                .unwrapData("Failed to get addresses")
                .map { addresses ->
                    addresses
                        .filter { it.deleteAt == null && it.address.deleteAt == null }
                        .map { it.toAddress() }
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getAddressesForRestaurant(): Result<List<Address>> {
        return try {
            addressApi.getAddresses()
                .unwrapData("Failed to get addresses")
                .map { addresses ->
                    addresses
                        .filter { it.deleteAt == null && it.address.deleteAt == null }
                        .map { it.toRestaurantAddress() }
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getAddressById(addressId: Int): Result<Address> {
        return try {
            addressApi.getAddress(addressId)
                .unwrapData("Failed to get address")
                .map { it.toAddress() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun addAddress(address: Address): Result<Unit> {
        return try {
            addressApi.addAddress(address.toCreateUserAddressRequest())
                .unwrapData("Failed to add address")
                .map { Unit }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun updateAddressDetails(address: Address): Result<Unit> {
        return try {
            addressApi.updateAddress(address.id, address.toUpdateUserAddressRequest())
                .unwrapData("Failed to update address")
                .map { Unit }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun updateAddressLocation(
        userAddressId: Int,
        placeTitle: String,
        fullText: String,
        latitude: Double,
        longitude: Double,
    ): Result<Unit> {
        return try {
            val body = Address(
                title = placeTitle,
                detail = fullText,
                latitude = latitude,
                longitude = longitude,
            ).toUpdateUserAddressLocationRequest()
            addressApi.updateAddressLocation(userAddressId, body)
                .unwrapData("Failed to update address location")
                .map { Unit }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun deleteAddress(addressId: Int): Result<Unit> {
        return try {
            addressApi.deleteAddress(addressId)
                .unwrapSuccess("Failed to delete address")
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
                    longitude = feature.geometry.coordinates[0],
                )
            }
            Result.success(addresses)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}
