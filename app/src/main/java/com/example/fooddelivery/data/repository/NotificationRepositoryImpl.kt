package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.local.room.dao.NotificationDao
import com.example.fooddelivery.data.local.room.entity.toDomain
import com.example.fooddelivery.data.local.room.entity.toEntity
import com.example.fooddelivery.data.remote.api.NotificationApi
import com.example.fooddelivery.data.remote.dto.toDomain
import com.example.fooddelivery.domain.model.Notification
import com.example.fooddelivery.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao,
    private val api: NotificationApi
) : NotificationRepository {

    override fun getNotifications(): Flow<List<Notification>> {
        return dao.getNotifications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUnreadCountFlow(): Flow<Int> {
        return dao.getUnreadCountFlow()
    }

    override suspend fun markAsRead(id: String) {
        try {
            val numericId = id.toIntOrNull()
            if (numericId != null) {
                api.markAsRead(numericId)
            }
            dao.markAsRead(id)
        } catch (e: Exception) {
            dao.markAsRead(id)
        }
    }

    override suspend fun markAllRead() {
        try {
            api.markAllRead()
            dao.markAllAsRead()
        } catch (e: Exception) {
            dao.markAllAsRead()
        }
    }

    override suspend fun deleteNotification(id: String) {
        try {
            val numericId = id.toIntOrNull()
            if (numericId != null) {
                api.deleteNotification(numericId)
            }
            dao.deleteNotification(id)
        } catch (e: Exception) {
            dao.deleteNotification(id)
        }
    }

    override suspend fun getNotificationsPaged(page: Int, pageSize: Int): Result<List<Notification>> {
        return try {
            val offset = (page - 1) * pageSize
            val response = api.getNotifications(limit = pageSize, offset = offset)
            val notifications = response.data.map { it.toDomain() }
            notifications.forEach { 
                dao.insertNotification(it.toEntity())
            }
            
            Result.success(notifications)
        } catch (e: Exception) {
            try {
                val offset = (page - 1) * pageSize
                val localNotifications = dao.getNotifications(pageSize, offset).map { it.toDomain() }
                if (localNotifications.isNotEmpty()) {
                    Result.success(localNotifications)
                } else {
                    Result.failure(e)
                }
            } catch (localEx: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getUnreadCount(): Result<Int> {
        return try {
            val response = api.getUnreadCount()
            Result.success(response.count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
