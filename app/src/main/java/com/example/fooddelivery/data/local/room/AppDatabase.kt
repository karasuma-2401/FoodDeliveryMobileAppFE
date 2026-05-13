package com.example.fooddelivery.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fooddelivery.data.local.room.dao.NotificationDao
import com.example.fooddelivery.data.local.room.entity.NotificationEntity

@Database(entities = [NotificationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val notificationDao: NotificationDao
    companion object {
        const val DATABASE_NAME = "food_delivery_db"
    }
}