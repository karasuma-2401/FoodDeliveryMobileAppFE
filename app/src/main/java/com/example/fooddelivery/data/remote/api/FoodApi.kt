package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.FoodResponse
import com.example.fooddelivery.data.remote.dto.IngredientDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodApi {
    @GET("food")
    suspend fun getFoods(
        @Query("categoryId") categoryId: Int? = null,
        @Query("restaurantId") restaurantId: Int? = null,
        @Query("name") keyword: String? = null,
        @Query("limit") limit: Int? = 100,
        @Query("offset") offset: Int? = 0
    ): Response<BaseResponse<List<FoodResponse>>>

    @GET("food/{id}")
    suspend fun getFoodById(@Path("id") id: Int): Response<BaseResponse<FoodResponse>>
    @GET("food/ingredients")
    suspend fun getIngredients(): Response<BaseResponse<List<IngredientDto>>>
}
