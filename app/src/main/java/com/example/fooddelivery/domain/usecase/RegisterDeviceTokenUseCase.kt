package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.DataStoreManager
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.DeviceRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RegisterDeviceTokenUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val dataStoreManager: DataStoreManager,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): Result<Unit> {
        if (!isEligibleForRegistration()) {
            return Result.success(Unit)
        }
        return runCatching {
            val token = fetchFcmToken()
            registerToken(token).getOrThrow()
        }
    }

    suspend fun registerToken(token: String): Result<Unit> {
        if (!isEligibleForRegistration()) {
            return Result.success(Unit)
        }
        return deviceRepository.registerDevice(token)
    }

    private suspend fun isEligibleForRegistration(): Boolean {
        val accessToken = tokenManager.getAccessToken.first()
        if (accessToken.isNullOrBlank()) return false
        return dataStoreManager.readNotificationsState().first()
    }

    private suspend fun fetchFcmToken(): String = suspendCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (token.isNullOrBlank()) {
                    continuation.resumeWithException(IllegalStateException("FCM token is empty"))
                } else {
                    continuation.resume(token)
                }
            } else {
                continuation.resumeWithException(
                    task.exception ?: IllegalStateException("Failed to get FCM token")
                )
            }
        }
    }
}
