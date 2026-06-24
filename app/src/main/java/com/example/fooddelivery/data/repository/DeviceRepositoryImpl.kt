package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.DeviceApi
import com.example.fooddelivery.data.remote.dto.DeviceRequest
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.domain.repository.DeviceRepository
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val api: DeviceApi
) : DeviceRepository {
    override suspend fun registerDevice(deviceToken: String): Result<Unit> {
        return try {
            api.registerDevice(DeviceRequest(deviceToken = deviceToken))
                .unwrapData("Failed to register device")
                .map { Unit }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
