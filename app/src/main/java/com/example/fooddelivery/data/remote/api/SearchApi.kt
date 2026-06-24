package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.*
import retrofit2.http.*

interface SearchApi {
    @GET("api/search")
    suspend fun unifiedSearch(
        @Query("q") query: String,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("sort") sort: String? = null,
        @Query("categoryId") categoryId: String? = null
    ): UnifiedSearchResponse

    @GET("api/search/suggestions")
    suspend fun getSuggestions(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("limit") limit: Int = 10
    ): UnifiedSearchResponse

    @GET("api/search/history")
    suspend fun getHistory(): SearchHistoryResponse

    @POST("api/search/history")
    suspend fun saveHistory(
        @Body request: Map<String, String> // {"keyword": "pizza"}
    ): SaveSearchHistoryResponse

    @DELETE("api/search/history")
    suspend fun clearAllHistory(): CommonResponse

    @DELETE("api/search/history/{id}")
    suspend fun deleteHistoryItem(
        @Path("id") id: Int
    ): CommonResponse
}
