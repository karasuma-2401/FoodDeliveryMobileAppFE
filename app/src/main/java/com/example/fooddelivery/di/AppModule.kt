package com.example.fooddelivery.di

import com.example.fooddelivery.data.remote.api.AuthApi
import com.example.fooddelivery.data.remote.api.UserApi
import com.example.fooddelivery.data.repository.AuthRepositoryImpl
import com.example.fooddelivery.data.repository.UserRepositoryImpl
import com.example.fooddelivery.domain.repository.AuthRepository
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
    fun provideAuthRepository(api: AuthApi): AuthRepository {
        return AuthRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideUserRepository(api: UserApi): UserRepository {
        return UserRepositoryImpl(api)
    }
}