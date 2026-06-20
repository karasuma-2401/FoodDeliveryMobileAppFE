package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(
        keyword: String? = null,
        limit: Int? = 50,
        offset: Int? = 0
    ): Result<List<Category>>
}
