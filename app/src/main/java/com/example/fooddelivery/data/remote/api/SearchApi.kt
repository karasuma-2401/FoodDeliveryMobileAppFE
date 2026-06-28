package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface SearchApi {
    @GET("search")
    suspend fun unifiedSearch(
        @Query("q") query: String,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("sort") sort: String? = null,
        @Query("categoryId") categoryId: String? = null,
    ): Response<BaseResponse<UnifiedSearchResponse>>

    @GET("search/suggestions")
    suspend fun getSuggestions(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("limit") limit: Int = 10,
    ): Response<BaseResponse<UnifiedSearchResponse>>

    @GET("search/history")
    suspend fun getHistory(): Response<BaseResponse<List<SearchHistoryDto>>>

    @POST("search/history")
    suspend fun saveHistory(
        @Body request: Map<String, String>,
    ): Response<BaseResponse<SearchHistoryDto>>

    @DELETE("search/history")
    suspend fun clearAllHistory(): Response<BaseResponse<Unit>>

    @DELETE("search/history/{id}")
    suspend fun deleteHistoryItem(
        @Path("id") id: Int,
    ): Response<BaseResponse<Unit>>

    @GET("search/trending")
    suspend fun getTrending(
        @Query("limit") limit: Int = 10,
    ): Response<BaseResponse<List<TrendingKeywordDto>>>
}
