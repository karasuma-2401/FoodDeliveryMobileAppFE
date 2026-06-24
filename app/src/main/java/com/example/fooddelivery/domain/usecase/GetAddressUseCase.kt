package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.repository.AddressRepository
import javax.inject.Inject

class GetAddressUseCase @Inject constructor(
    private val repository: AddressRepository
) {
    suspend operator fun invoke(addressId: Int): Result<Address> {
        return repository.getAddressById(addressId)
    }
}
