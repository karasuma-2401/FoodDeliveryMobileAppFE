package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.repository.AddressRepository
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val repository: AddressRepository
) {
    suspend operator fun invoke(query: String): Result<List<Address>> {
        return repository.searchPlaces(query)
    }
}
