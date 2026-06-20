package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.repository.RestaurantRepository
import javax.inject.Inject

class GetRestaurantsUseCase @Inject constructor(
    private val repository: RestaurantRepository
) {
    suspend operator fun invoke(
        limit: Int? = 20,
        offset: Int? = 0,
        keyword: String? = null,
        categoryId: Int? = null
    ): Result<List<Restaurant>> {
        return repository.getRestaurants(limit, offset, keyword, categoryId)
    }
}
