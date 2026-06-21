package com.example.fooddelivery.di

import com.example.fooddelivery.data.remote.api.AdminApi
import com.example.fooddelivery.data.repository.AdminRepositoryImpl
import com.example.fooddelivery.domain.repository.AdminRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdminModule {

    @Provides
    @Singleton
    fun provideAdminApi(@Named("MainRetrofit") retrofit: Retrofit): AdminApi {
        return retrofit.create(AdminApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAdminRepository(api: AdminApi): AdminRepository {
        return AdminRepositoryImpl(api)
    }
}

