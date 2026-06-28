package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.Address

interface AddressRepository {
    suspend fun getAddressesForRestaurant(): Result<List<Address>>
    suspend fun getAddresses(): Result<List<Address>>
    suspend fun getAddressById(addressId: Int): Result<Address>
    suspend fun addAddress(address: Address): Result<Unit>
    suspend fun updateAddressDetails(address: Address): Result<Unit>
    suspend fun updateAddressLocation(
        userAddressId: Int,
        placeTitle: String,
        fullText: String,
        latitude: Double,
        longitude: Double,
    ): Result<Unit>
    suspend fun deleteAddress(addressId: Int): Result<Unit>
    suspend fun searchPlaces(query: String): Result<List<Address>>
}
