package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.DeviceRequest
import com.example.fooddelivery.data.remote.dto.DeviceResponse
import com.example.fooddelivery.data.remote.dto.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DeviceApi {
    @POST("device")
    suspend fun registerDevice(@Body request: DeviceRequest): Response<BaseResponse<DeviceResponse>>
}
