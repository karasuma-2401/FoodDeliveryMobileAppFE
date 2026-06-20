package com.example.fooddelivery.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fooddelivery.data.local.room.dao.CartDao
import com.example.fooddelivery.data.local.room.dao.ConversationDao
import com.example.fooddelivery.data.local.room.dao.MessageDao
import com.example.fooddelivery.data.local.room.dao.NotificationDao
import com.example.fooddelivery.data.local.room.entity.CartEntity
import com.example.fooddelivery.data.local.room.entity.ConversationEntity
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.data.local.room.entity.NotificationEntity

@Database(
    entities = [
        NotificationEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        CartEntity::class,
    ],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract val notificationDao: NotificationDao
    abstract val messageDao: MessageDao
    abstract val conversationDao: ConversationDao
    abstract val cartDao: CartDao

    companion object {
        const val DATABASE_NAME = "food_delivery_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        isRead INTEGER NOT NULL,
                        targetId TEXT
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS messages (
                        id TEXT PRIMARY KEY NOT NULL,
                        conversationId TEXT NOT NULL,
                        senderId TEXT NOT NULL,
                        content TEXT NOT NULL,
                        imageUrl TEXT,
                        createdAt TEXT NOT NULL,
                        isSending INTEGER NOT NULL,
                        isFailed INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS conversations (
                        id TEXT PRIMARY KEY NOT NULL,
                        restaurantName TEXT NOT NULL,
                        restaurantImage TEXT NOT NULL,
                        lastMessage TEXT NOT NULL,
                        lastMessageTime TEXT NOT NULL,
                        unreadCount INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cart_items` (
                        `foodId` TEXT NOT NULL, 
                        `size` TEXT NOT NULL, 
                        `restaurantId` TEXT NOT NULL, 
                        `quantity` INTEGER NOT NULL, 
                        `unitPrice` REAL NOT NULL, 
                        `restaurantName` TEXT NOT NULL, 
                        `foodName` TEXT NOT NULL, 
                        `foodPrice` REAL NOT NULL, 
                        `foodImageUrl` TEXT, 
                        `foodImageRes` INTEGER, 
                        `categoryId` TEXT NOT NULL, 
                        `rating` REAL NOT NULL, 
                        `reviewCount` INTEGER NOT NULL, 
                        `soldCount` INTEGER NOT NULL, 
                        `promoTag` TEXT, 
                        PRIMARY KEY(`foodId`, `size`, `restaurantId`)
                    )
                """.trimIndent())
            }
        }
    }
}
