package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.AddressRequest
import com.example.fooddelivery.data.remote.dto.AddressResponse
import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.DeleteAddressResponse
import retrofit2.Response
import retrofit2.http.*

interface AddressApi {
    @GET("user/address/all")
    suspend fun getAddresses(): Response<BaseResponse<List<AddressResponse>>>

    @GET("user/address/{addressId}")
    suspend fun getAddress(@Path("addressId") addressId: Int): Response<BaseResponse<AddressResponse>>

    @POST("user/address")
    suspend fun addAddress(@Body address: AddressRequest): Response<BaseResponse<AddressResponse>>

    @PUT("user/address/{id}")
    suspend fun updateAddress(@Path("id") id: Int, @Body address: AddressRequest): Response<BaseResponse<AddressResponse>>

    @DELETE("user/address/{addressId}")
    suspend fun deleteAddress(@Path("addressId") addressId: Int): Response<BaseResponse<DeleteAddressResponse>>
}
