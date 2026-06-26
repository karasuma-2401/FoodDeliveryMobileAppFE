package com.example.fooddelivery.di

import android.content.Context
import androidx.room.Room
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.local.datastore.DataStoreManager
import com.example.fooddelivery.data.local.room.AppDatabase
import com.example.fooddelivery.data.local.room.dao.CartDao
import com.example.fooddelivery.data.local.room.dao.NotificationDao
import com.example.fooddelivery.data.local.room.dao.ConversationDao
import com.example.fooddelivery.data.local.room.dao.MessageDao
import com.example.fooddelivery.data.remote.api.AddressApi
import com.example.fooddelivery.data.remote.api.AuthApi
import com.example.fooddelivery.data.remote.api.CartApi
import com.example.fooddelivery.data.remote.api.CategoryApi
import com.example.fooddelivery.data.remote.api.ChatApi
import com.example.fooddelivery.data.remote.api.DeviceApi
import com.example.fooddelivery.data.remote.api.FoodApi
import com.example.fooddelivery.data.remote.api.HomeApi
import com.example.fooddelivery.data.remote.api.NotificationApi
import com.example.fooddelivery.data.remote.api.OrderApi
import com.example.fooddelivery.data.remote.api.PaymentApi
import com.example.fooddelivery.data.remote.api.PhotonService
import com.example.fooddelivery.data.remote.api.SearchApi
import com.example.fooddelivery.data.remote.api.UserApi
import com.example.fooddelivery.data.remote.api.RestaurantApi
import com.example.fooddelivery.data.remote.api.VoucherApi
import com.example.fooddelivery.data.repository.AddressRepositoryImpl
import com.example.fooddelivery.data.repository.AuthRepositoryImpl
import com.example.fooddelivery.data.repository.CartRepositoryImpl
import com.example.fooddelivery.data.repository.DeliveryLocationRepositoryImpl
import com.example.fooddelivery.data.repository.CategoryRepositoryImpl
import com.example.fooddelivery.data.repository.ChatRepositoryImpl
import com.example.fooddelivery.data.repository.DeviceRepositoryImpl
import com.example.fooddelivery.data.repository.FoodRepositoryImpl
import com.example.fooddelivery.data.repository.HomeRepositoryImpl
import com.example.fooddelivery.data.repository.NotificationRepositoryImpl
import com.example.fooddelivery.data.repository.OrderRepositoryImpl
import com.example.fooddelivery.data.repository.PaymentRepositoryImpl
import com.example.fooddelivery.data.repository.RestaurantRepositoryImpl
import com.example.fooddelivery.data.repository.SearchRepositoryImpl
import com.example.fooddelivery.data.repository.UserRepositoryImpl
import com.example.fooddelivery.data.repository.VoucherRepositoryImpl
import com.example.fooddelivery.domain.repository.AddressRepository
import com.example.fooddelivery.domain.repository.AuthRepository
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.DeliveryLocationRepository
import com.example.fooddelivery.domain.repository.CategoryRepository
import com.example.fooddelivery.domain.repository.ChatRepository
import com.example.fooddelivery.domain.repository.DeviceRepository
import com.example.fooddelivery.domain.repository.FoodRepository
import com.example.fooddelivery.domain.repository.HomeRepository
import com.example.fooddelivery.domain.repository.NotificationRepository
import com.example.fooddelivery.domain.repository.OrderRepository
import com.example.fooddelivery.domain.repository.PaymentRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.repository.SearchRepository
import com.example.fooddelivery.domain.repository.UserRepository
import com.example.fooddelivery.domain.repository.VoucherRepository
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_4,
                AppDatabase.MIGRATION_6_7,
            )
            .fallbackToDestructiveMigration()
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
        database: AppDatabase,
        @ApplicationContext context: Context
    ): UserRepository {
        return UserRepositoryImpl(api, tokenManager, database, context)
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
    fun provideDeliveryLocationRepository(
        addressRepository: AddressRepository,
        dataStoreManager: DataStoreManager,
    ): DeliveryLocationRepository {
        return DeliveryLocationRepositoryImpl(addressRepository, dataStoreManager)
    }

    @Provides
    @Singleton
    fun provideCartRepository(
        dao: CartDao,
        api: CartApi
    ) : CartRepository {
        return CartRepositoryImpl(dao, api)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        api: CategoryApi,
        @ApplicationContext context: Context
    ): CategoryRepository {
        return CategoryRepositoryImpl(api, context)
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
        api: OrderApi,
        cartRepository: CartRepository,
    ): OrderRepository {
        return OrderRepositoryImpl(api, cartRepository)
    }

    @Provides
    @Singleton
    fun providePaymentRepository(
        api: PaymentApi
    ): PaymentRepository {
        return PaymentRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        dao: NotificationDao,
        api: NotificationApi
    ): NotificationRepository {
        return NotificationRepositoryImpl(dao,api)
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(
        api: DeviceApi
    ): DeviceRepository {
        return DeviceRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideFoodRepository(
        api: FoodApi
    ): FoodRepository {
        return FoodRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideHomeRepository(
        api: HomeApi
    ): HomeRepository {
        return HomeRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(
        api: SearchApi
    ): SearchRepository {
        return SearchRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        chatApi: ChatApi,
        conversationDao: ConversationDao,
        messageDao: MessageDao,
        socket: Socket,
        tokenManager: TokenManager
    ): ChatRepository {
        return ChatRepositoryImpl(chatApi, conversationDao, messageDao, socket, tokenManager)
    }

    @Provides
    @Singleton
    fun provideVoucherRepository(
        api: VoucherApi
    ): VoucherRepository {
        return VoucherRepositoryImpl(api)
    }
}
