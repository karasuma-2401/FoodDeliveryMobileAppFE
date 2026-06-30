package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.NotificationRepository
import javax.inject.Inject

class SyncNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(): Result<Unit> = notificationRepository.syncNotifications()
}
