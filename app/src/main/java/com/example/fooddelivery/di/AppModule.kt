package com.example.fooddelivery.di

import com.example.fooddelivery.data.remote.api.AuthApi
import com.example.fooddelivery.data.remote.api.RestaurantApi
import com.example.fooddelivery.domain.repository.AuthRepository
import com.example.fooddelivery.data.repository.AuthRepositoryImpl
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.data.repository.RestaurantRepositoryImpl
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
    fun provideAuthRepository(api: AuthApi): AuthRepository {
        return AuthRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideRestaurantRepository(api: RestaurantApi): RestaurantRepository {
        return RestaurantRepositoryImpl(api)
    }
}
