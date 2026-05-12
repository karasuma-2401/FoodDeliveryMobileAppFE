package com.example.fooddelivery.di

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.remote.api.AddressApi
import com.example.fooddelivery.data.remote.api.AuthApi
import com.example.fooddelivery.data.remote.api.OrderApi
import com.example.fooddelivery.data.remote.api.PhotonService
import com.example.fooddelivery.data.remote.api.UserApi
import com.example.fooddelivery.data.remote.api.RestaurantApi
import com.example.fooddelivery.data.repository.AddressRepositoryImpl
import com.example.fooddelivery.data.repository.AuthRepositoryImpl
import com.example.fooddelivery.data.repository.CartRepositoryImpl
import com.example.fooddelivery.data.repository.OrderRepositoryImpl
import com.example.fooddelivery.data.repository.RestaurantRepositoryImpl
import com.example.fooddelivery.data.repository.UserRepositoryImpl
import com.example.fooddelivery.domain.repository.AddressRepository
import com.example.fooddelivery.domain.repository.AuthRepository
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.OrderRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
        tokenManager: TokenManager
    ): UserRepository {
        return UserRepositoryImpl(api, tokenManager)
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
    fun provideCartRepository() : CartRepository {
        return CartRepositoryImpl()
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
}
