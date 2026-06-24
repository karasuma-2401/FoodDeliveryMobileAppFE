package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.SearchApi
import com.example.fooddelivery.data.remote.dto.*
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.SearchHistory
import com.example.fooddelivery.domain.model.SearchSortOption
import com.example.fooddelivery.domain.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val searchApi: SearchApi
) : SearchRepository {

    override suspend fun unifiedSearch(
        query: String,
        lat: Double?,
        lng: Double?,
        limit: Int,
        offset: Int,
        sort: SearchSortOption?,
        categoryId: String?
    ): Result<Pair<List<FoodItem>, List<Restaurant>>> {
        return try {
            val response = searchApi.unifiedSearch(
                query = query,
                lat = lat,
                lng = lng,
                limit = limit,
                offset = offset,
                sort = sort?.apiValue,
                categoryId = categoryId
            )
            val foods = response.foods.map { it.toDomain() }
            val restaurants = response.restaurants.map { it.toDomain() }
            Result.success(Pair(foods, restaurants))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSuggestions(
        lat: Double?,
        lng: Double?,
        limit: Int
    ): Result<Pair<List<FoodItem>, List<Restaurant>>> {
        return try {
            val response = searchApi.getSuggestions(lat, lng, limit)
            val foods = response.foods.map { it.toDomain() }
            val restaurants = response.restaurants.map { it.toDomain() }
            Result.success(Pair(foods, restaurants))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHistory(): Result<List<SearchHistory>> {
        return try {
            val response = searchApi.getHistory()
            if (response.success) {
                Result.success(response.data.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to get history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveHistory(keyword: String): Result<SearchHistory> {
        return try {
            val response = searchApi.saveHistory(mapOf("keyword" to keyword))
            if (response.success) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.message ?: "Failed to save history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllHistory(): Result<Unit> {
        return try {
            val response = searchApi.clearAllHistory()
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to clear history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteHistoryItem(id: Int): Result<Unit> {
        return try {
            val response = searchApi.deleteHistoryItem(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete history item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
