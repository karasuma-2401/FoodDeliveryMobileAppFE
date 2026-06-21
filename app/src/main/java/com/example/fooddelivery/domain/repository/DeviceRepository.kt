package com.example.fooddelivery.domain.repository

interface DeviceRepository {
    suspend fun registerDevice(deviceToken: String): Result<Unit>
}
