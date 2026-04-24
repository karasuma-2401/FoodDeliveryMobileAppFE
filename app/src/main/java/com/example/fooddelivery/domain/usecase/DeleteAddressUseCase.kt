package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.AddressRepository
import javax.inject.Inject

class DeleteAddressUseCase @Inject constructor(
    private val repository: AddressRepository
) {
    suspend operator fun invoke(addressId: String): Result<Unit> {
        return repository.deleteAddress(addressId)
    }
}
