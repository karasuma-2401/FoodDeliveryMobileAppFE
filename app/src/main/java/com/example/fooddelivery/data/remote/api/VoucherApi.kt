package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface VoucherApi {
    @GET("vouchers")
    suspend fun getVouchers(
        @Query("limit") limit: Int? = 20,
        @Query("offset") offset: Int? = 0,
        @Query("restaurantId") restaurantId: Int? = null,
        @Query("code") code: String? = null,
        @Query("status") status: String? = null
    ): Response<BaseResponse<VoucherListResponseDto>>

    @GET("vouchers/suitable/{restaurantId}")
    suspend fun getSuitableVouchers(
        @Path("restaurantId") restaurantId: Int,
        @Query("cost") cost: Double? = null
    ): Response<List<VoucherDto>>

    @GET("vouchers/code/{code}")
    suspend fun getVoucherByCode(
        @Path("code") code: String,
        @Query("restaurantId") restaurantId: Int? = null
    ): Response<VoucherDto>

    @GET("vouchers/{id}")
    suspend fun getVoucherById(
        @Path("id") id: Int
    ): Response<VoucherDto>

    @Multipart
    @POST("vouchers")
    suspend fun createVoucher(
        @Part("name") name: RequestBody,
        @Part("code") code: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("sale") sale: RequestBody,
        @Part("type") type: RequestBody,
        @Part("status") status: RequestBody?,
        @Part("restaurantId") restaurantId: RequestBody?,
        @Part("minimumOrderAmount") minimumOrderAmount: RequestBody?,
        @Part("maximumDiscountAmount") maximumDiscountAmount: RequestBody?,
        @Part("startAt") startAt: RequestBody?,
        @Part("endAt") endAt: RequestBody?,
        @Part("usageLimit") usageLimit: RequestBody? = null,
        @Part("userLimit") userLimit: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): Response<BaseResponse<VoucherDto>>

    @Multipart
    @PATCH("vouchers/{id}")
    suspend fun updateVoucher(
        @Path("id") id: Int,
        @Part("name") name: RequestBody? = null,
        @Part("code") code: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("sale") sale: RequestBody? = null,
        @Part("type") type: RequestBody? = null,
        @Part("status") status: RequestBody? = null,
        @Part("minimumOrderAmount") minimumOrderAmount: RequestBody? = null,
        @Part("maximumDiscountAmount") maximumDiscountAmount: RequestBody? = null,
        @Part("startAt") startAt: RequestBody? = null,
        @Part("endAt") endAt: RequestBody? = null,
        @Part("usageLimit") usageLimit: RequestBody? = null,
        @Part("userLimit") userLimit: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): Response<VoucherDto>

    @DELETE("vouchers/{id}")
    suspend fun endVoucher(@Path("id") id: Int): Response<VoucherDto>
}
