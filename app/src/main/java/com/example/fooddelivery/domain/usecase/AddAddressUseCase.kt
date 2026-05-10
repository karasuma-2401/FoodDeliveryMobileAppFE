package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.repository.AddressRepository
import javax.inject.Inject

class AddAddressUseCase @Inject constructor(
    private val repository: AddressRepository
) {
    suspend operator fun invoke(address: Address): Result<Unit> {
        return repository.addAddress(address)
    }
}
