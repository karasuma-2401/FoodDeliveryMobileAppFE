package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.AddressResponse
import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.CreateUserAddressRequest
import com.example.fooddelivery.data.remote.dto.DeleteAddressResponse
import com.example.fooddelivery.data.remote.dto.UpdateUserAddressLocationRequest
import com.example.fooddelivery.data.remote.dto.UpdateUserAddressRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AddressApi {
    @GET("user/address/all")
    suspend fun getAddresses(): Response<BaseResponse<List<AddressResponse>>>

    @GET("user/address/{addressId}")
    suspend fun getAddress(@Path("addressId") addressId: Int): Response<BaseResponse<AddressResponse>>

    @POST("user/address")
    suspend fun addAddress(@Body address: CreateUserAddressRequest): Response<BaseResponse<AddressResponse>>

    @PUT("user/address/{id}")
    suspend fun updateAddress(
        @Path("id") id: Int,
        @Body address: UpdateUserAddressRequest,
    ): Response<BaseResponse<AddressResponse>>

    @PUT("user/address/{id}/location")
    suspend fun updateAddressLocation(
        @Path("id") id: Int,
        @Body body: UpdateUserAddressLocationRequest,
    ): Response<BaseResponse<AddressResponse>>

    @DELETE("user/address/{addressId}")
    suspend fun deleteAddress(@Path("addressId") addressId: Int): Response<BaseResponse<DeleteAddressResponse>>
}
