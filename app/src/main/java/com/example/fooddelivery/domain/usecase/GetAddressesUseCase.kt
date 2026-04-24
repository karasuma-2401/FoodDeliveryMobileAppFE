package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.repository.AddressRepository
import javax.inject.Inject

class GetAddressesUseCase @Inject constructor(
    private val repository: AddressRepository
) {
    suspend operator fun invoke(): Result<List<Address>> {
        return repository.getAddresses()
    }
}
