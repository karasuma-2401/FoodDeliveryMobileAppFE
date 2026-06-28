package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.RestaurantListQuery
import com.example.fooddelivery.domain.repository.RestaurantRepository
import javax.inject.Inject

class GetRestaurantsUseCase @Inject constructor(
    private val repository: RestaurantRepository
) {
    suspend operator fun invoke(query: RestaurantListQuery): Result<List<Restaurant>> {
        return repository.getRestaurants(query)
    }
}
