package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.SearchHistory
import com.example.fooddelivery.domain.model.SearchSortOption
import com.example.fooddelivery.domain.model.TrendingKeyword

interface SearchRepository {
    suspend fun unifiedSearch(
        query: String,
        lat: Double? = null,
        lng: Double? = null,
        limit: Int = 20,
        offset: Int = 0,
        sort: SearchSortOption? = null,
        categoryId: String? = null,
    ): Result<Pair<List<FoodItem>, List<Restaurant>>>

    suspend fun getSuggestions(
        lat: Double? = null,
        lng: Double? = null,
        limit: Int = 10,
    ): Result<Pair<List<FoodItem>, List<Restaurant>>>

    suspend fun getTrending(
        limit: Int = 10,
    ): Result<List<TrendingKeyword>>

    suspend fun getHistory(): Result<List<SearchHistory>>

    suspend fun saveHistory(keyword: String): Result<SearchHistory>

    suspend fun clearAllHistory(): Result<Unit>

    suspend fun deleteHistoryItem(id: Int): Result<Unit>
}
