package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.SearchApi
import com.example.fooddelivery.data.remote.dto.toDomain
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.data.remote.unwrapUnit
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.SearchHistory
import com.example.fooddelivery.domain.model.SearchSortOption
import com.example.fooddelivery.domain.model.TrendingKeyword
import com.example.fooddelivery.domain.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val searchApi: SearchApi,
) : SearchRepository {

    override suspend fun unifiedSearch(
        query: String,
        lat: Double?,
        lng: Double?,
        limit: Int,
        offset: Int,
        sort: SearchSortOption?,
        categoryId: String?,
    ): Result<Pair<List<FoodItem>, List<Restaurant>>> {
        return try {
            searchApi.unifiedSearch(
                query = query,
                lat = lat,
                lng = lng,
                limit = limit,
                offset = offset,
                sort = sort?.apiValue,
                categoryId = categoryId,
            ).unwrapData("Failed to search")
                .map { it.toDomain() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSuggestions(
        lat: Double?,
        lng: Double?,
        limit: Int,
    ): Result<Pair<List<FoodItem>, List<Restaurant>>> {
        return try {
            searchApi.getSuggestions(lat, lng, limit)
                .unwrapData("Failed to load suggestions")
                .map { it.toDomain() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrending(limit: Int): Result<List<TrendingKeyword>> {
        return try {
            searchApi.getTrending(limit)
                .unwrapData("Failed to load trending keywords")
                .map { list -> list.map { it.toDomain() } }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHistory(): Result<List<SearchHistory>> {
        return try {
            searchApi.getHistory()
                .unwrapData("Failed to get history")
                .map { list -> list.map { it.toDomain() } }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveHistory(keyword: String): Result<SearchHistory> {
        return try {
            searchApi.saveHistory(mapOf("keyword" to keyword))
                .unwrapData("Failed to save history")
                .map { it.toDomain() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllHistory(): Result<Unit> {
        return try {
            searchApi.clearAllHistory()
                .unwrapUnit("Failed to clear history")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteHistoryItem(id: Int): Result<Unit> {
        return try {
            searchApi.deleteHistoryItem(id)
                .unwrapUnit("Failed to delete history item")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
