package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.Address

interface AddressRepository {
    suspend fun getAddresses(): Result<List<Address>>
    suspend fun addAddress(address: Address): Result<Unit>
    suspend fun updateAddress(address: Address): Result<Unit>
    suspend fun deleteAddress(addressId: String): Result<Unit>
}
