package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.local.room.dao.NotificationDao
import com.example.fooddelivery.data.local.room.entity.toDomain
import com.example.fooddelivery.domain.model.Notification
import com.example.fooddelivery.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(): Flow<List<Notification>> {
        return dao.getNotifications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun markAsRead(id: String) {
        dao.markAsRead(id)
    }

    override suspend fun markAllRead() {
        dao.markAllAsRead()
    }

    override suspend fun deleteNotification(id: String) {
        dao.deleteNotification(id)
    }

    override suspend fun getNotificationsPaged(page: Int, pageSize: Int): Result<List<Notification>> {
        return try {
            val offset = (page - 1) * pageSize
            val notifications = dao.getNotifications(pageSize, offset).map { it.toDomain() }
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
