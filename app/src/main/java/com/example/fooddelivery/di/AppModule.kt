package com.example.fooddelivery.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.local.room.AppDatabase
import com.example.fooddelivery.data.local.room.dao.CartDao
import com.example.fooddelivery.data.local.room.dao.NotificationDao
import com.example.fooddelivery.data.local.room.dao.ConversationDao
import com.example.fooddelivery.data.local.room.dao.MessageDao
import com.example.fooddelivery.data.remote.api.AddressApi
import com.example.fooddelivery.data.remote.api.AuthApi
import com.example.fooddelivery.data.remote.api.ChatApi
import com.example.fooddelivery.data.remote.api.OrderApi
import com.example.fooddelivery.data.remote.api.PhotonService
import com.example.fooddelivery.data.remote.api.UserApi
import com.example.fooddelivery.data.remote.api.RestaurantApi
import com.example.fooddelivery.data.repository.AddressRepositoryImpl
import com.example.fooddelivery.data.repository.AuthRepositoryImpl
import com.example.fooddelivery.data.repository.CartRepositoryImpl
import com.example.fooddelivery.data.repository.ChatRepositoryImpl
import com.example.fooddelivery.data.repository.NotificationRepositoryImpl
import com.example.fooddelivery.data.repository.OrderRepositoryImpl
import com.example.fooddelivery.data.repository.RestaurantRepositoryImpl
import com.example.fooddelivery.data.repository.UserRepositoryImpl
import com.example.fooddelivery.domain.repository.AddressRepository
import com.example.fooddelivery.domain.repository.AuthRepository
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.ChatRepository
import com.example.fooddelivery.domain.repository.NotificationRepository
import com.example.fooddelivery.domain.repository.OrderRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.socket.client.Socket
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create notifications table if it doesn't exist
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

            // Create messages table if it doesn't exist
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

            // Create conversations table if it doesn't exist
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao

    @Provides
    fun provideConversationDao(db: AppDatabase): ConversationDao = db.conversationDao

    @Provides
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao

    @Provides
    fun provideCartDao(db: AppDatabase): CartDao = db.cartDao

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AuthApi,
    ): AuthRepository {
        return AuthRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        api: UserApi,
        tokenManager: TokenManager,
        database: AppDatabase
    ): UserRepository {
        return UserRepositoryImpl(api, tokenManager, database)
    }

    @Provides
    @Singleton
    fun provideAddressRepository(
        addressApi: AddressApi,
        photonService: PhotonService
    ): AddressRepository {
        return AddressRepositoryImpl(addressApi, photonService)
    }

    @Provides
    @Singleton
    fun provideCartRepository(
        dao: CartDao
    ) : CartRepository {
        return CartRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideRestaurantRepository(
        api: RestaurantApi
    ): RestaurantRepository {
        return RestaurantRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(
        api: OrderApi
    ): OrderRepository {
        return OrderRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        dao: NotificationDao
    ): NotificationRepository {
        return NotificationRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        chatApi: ChatApi,
        conversationDao: ConversationDao,
        messageDao: MessageDao,
        socket: Socket
    ): ChatRepository {
        return ChatRepositoryImpl(chatApi, conversationDao, messageDao, socket)
    }
}
