package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(
        keyword: String? = null,
        limit: Int? = 50,
        offset: Int? = 0
    ): Result<List<Category>> {
        return repository.getCategories(keyword, limit, offset)
    }
}
