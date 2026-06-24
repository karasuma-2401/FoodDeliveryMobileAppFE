package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.CategoryApi
import com.example.fooddelivery.data.remote.parseErrorMessage
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
            val response = api.getCategories(keyword, limit, offset)
            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                val categories = baseResponse.data?.map { dto ->
                    Category(
                        id = dto.id.toString(),
                        name = dto.name,
                        imageUrl = dto.image,
                        description = dto.description,
                        foodCount = dto.foodCount ?: 0
                    )
                } ?: emptyList()

                Result.success(categories)
            } else {
                Result.failure(Exception(response.parseErrorMessage("Failed to load categories")))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }
}