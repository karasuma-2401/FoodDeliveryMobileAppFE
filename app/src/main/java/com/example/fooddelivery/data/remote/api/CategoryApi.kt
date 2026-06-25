package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.BaseListResponse
import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.CategoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface CategoryApi {
    @GET("categories")
    suspend fun getCategories(
        @Query("keyword") keyword: String? = null,
        @Query("limit") limit: Int? = 50,
        @Query("offset") offset: Int? = 0,
        @Query("isActive") isActive: Boolean? = null
    ): Response<BaseResponse<BaseListResponse<CategoryResponse>>>

    @GET("categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Int): Response<BaseResponse<CategoryResponse>>

    @Multipart
    @POST("categories")
    suspend fun createCategory(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("displayOrder") displayOrder: RequestBody? = null,
        @Part("isActive") isActive: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): Response<BaseResponse<CategoryResponse>>

    @Multipart
    @PATCH("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Part("name") name: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("displayOrder") displayOrder: RequestBody? = null,
        @Part("isActive") isActive: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): Response<BaseResponse<CategoryResponse>>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<BaseResponse<Unit>>
}