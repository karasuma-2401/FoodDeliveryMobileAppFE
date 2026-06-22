package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.FoodResponse
import com.example.fooddelivery.data.remote.dto.BaseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodApi {
    @GET("api/food")
    suspend fun getFoods(
        @Query("categoryId") categoryId: Int? = null,
        @Query("restaurantId") restaurantId: Int? = null,
        @Query("keyword") keyword: String? = null
    ): Response<List<FoodResponse>>

    @GET("api/food/{id}")
    suspend fun getFoodById(@Path("id") id: Int): Response<FoodResponse>
}
