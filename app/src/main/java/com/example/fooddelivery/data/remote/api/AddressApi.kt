package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.domain.model.Address
import retrofit2.Response
import retrofit2.http.*

interface AddressApi {
    @GET("user/addresses")
    suspend fun getAddresses(): Response<List<Address>>

    @POST("user/addresses")
    suspend fun addAddress(@Body address: Address): Response<Unit>

    @PUT("user/addresses/{id}")
    suspend fun updateAddress(@Path("id") id: String, @Body address: Address): Response<Unit>

    @DELETE("user/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): Response<Unit>
}
