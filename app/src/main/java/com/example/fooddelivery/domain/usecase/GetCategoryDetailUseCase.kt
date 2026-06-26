package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.model.CategoryDetail
import com.example.fooddelivery.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoryDetailUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(id: Int): Result<CategoryDetail> {
        return repository.getCategoryById(id)
    }
}
