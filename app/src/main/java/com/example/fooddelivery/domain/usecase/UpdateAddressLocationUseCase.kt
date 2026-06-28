package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.AddressRepository
import javax.inject.Inject

class UpdateAddressLocationUseCase @Inject constructor(
    private val repository: AddressRepository,
) {
    suspend operator fun invoke(
        userAddressId: Int,
        placeTitle: String,
        fullText: String,
        latitude: Double,
        longitude: Double,
    ): Result<Unit> {
        return repository.updateAddressLocation(
            userAddressId = userAddressId,
            placeTitle = placeTitle,
            fullText = fullText,
            latitude = latitude,
            longitude = longitude,
        )
    }
}
