package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.CategoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CategoryApi {
    @GET("categories")
    suspend fun getCategories(
        @Query("keyword") keyword: String? = null,
        @Query("limit") limit: Int? = 50,
        @Query("offset") offset: Int? = 0
    ): Response<List<CategoryResponse>>
}
