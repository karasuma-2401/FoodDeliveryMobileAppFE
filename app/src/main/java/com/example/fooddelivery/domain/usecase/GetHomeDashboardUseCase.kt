package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.HomeDashboardData
import com.example.fooddelivery.domain.repository.HomeRepository
import javax.inject.Inject

class GetHomeDashboardUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(lat: Double? = null, lng: Double? = null): Result<HomeDashboardData> {
        return repository.getHomeDashboard(lat, lng)
    }
}
