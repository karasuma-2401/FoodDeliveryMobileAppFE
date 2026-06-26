package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.CategoryDetail

interface CategoryRepository {
    suspend fun getCategories(
        keyword: String? = null,
        limit: Int? = 50,
        offset: Int? = 0,
        isActive: Boolean? = null
    ): Result<List<Category>>

    suspend fun getCategoryById(id: Int): Result<CategoryDetail>

    suspend fun createCategory(
        name: String,
        description: String,
        displayOrder: Int,
        isActive: Boolean,
        imageUri: String? = null
    ): Result<Category>

    suspend fun updateCategory(
        id: Int,
        name: String? = null,
        description: String? = null,
        displayOrder: Int? = null,
        isActive: Boolean? = null,
        imageUri: String? = null
    ): Result<Category>

    suspend fun deleteCategory(id: Int): Result<Unit>
}
