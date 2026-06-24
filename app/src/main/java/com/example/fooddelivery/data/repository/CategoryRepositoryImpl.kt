package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.CategoryApi
import com.example.fooddelivery.data.remote.unwrapList
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.repository.CategoryRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class CategoryRepositoryImpl @Inject constructor(
    private val api: CategoryApi
) : CategoryRepository {
    override suspend fun getCategories(
        keyword: String?,
        limit: Int?,
        offset: Int?
    ): Result<List<Category>> {
        return try {
            api.getCategories(keyword, limit, offset)
                .unwrapList("Failed to load categories")
                .map { list ->
                    list.map { dto ->
                        Category(
                            id = dto.id.toString(),
                            name = dto.name,
                            imageUrl = dto.image,
                            description = dto.description,
                            foodCount = dto.foodCount ?: 0
                        )
                    }
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }
}
