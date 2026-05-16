package com.example.fooddelivery.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fooddelivery.data.local.room.dao.ConversationDao
import com.example.fooddelivery.data.local.room.dao.MessageDao
import com.example.fooddelivery.data.local.room.dao.NotificationDao
import com.example.fooddelivery.data.local.room.entity.ConversationEntity
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.data.local.room.entity.NotificationEntity

@Database(
    entities = [
        NotificationEntity::class,
        MessageEntity::class,
        ConversationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val notificationDao: NotificationDao
    abstract val messageDao: MessageDao
    abstract val conversationDao: ConversationDao

    companion object {
        const val DATABASE_NAME = "food_delivery_db"
    }
}