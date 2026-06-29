package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications() : Flow<List<Notification>>
    fun getUnreadCountFlow() : Flow<Int>
    suspend fun markAsRead(id: String)
    suspend fun markAllRead()
    suspend fun deleteNotification(id: String)
    suspend fun getNotificationsPaged(page: Int, pageSize: Int): Result<List<Notification>>
    suspend fun getUnreadCount(): Result<Int>
    suspend fun syncNotifications(): Result<Unit>
}