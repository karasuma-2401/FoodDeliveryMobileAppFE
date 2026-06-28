package com.example.fooddelivery.di

import com.example.fooddelivery.BuildConfig
import com.example.fooddelivery.data.local.datastore.TokenManager
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
import com.example.fooddelivery.data.remote.api.UserApi
import com.example.fooddelivery.data.remote.api.RestaurantApi
import com.example.fooddelivery.data.remote.api.SearchApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.example.fooddelivery.data.remote.api.VoucherApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val path = request.url.encodedPath
            val requestBuilder = request.newBuilder()
            val token = tokenManager.bearerToken()
            if (
                shouldAttachAuthToken(path) &&
                !token.isNullOrBlank() &&
                request.header("Authorization") == null
            ) {
                requestBuilder.header("Authorization", token)
            }
            chain.proceed(requestBuilder.build())
        }
    }

    private fun shouldAttachAuthToken(path: String): Boolean {
        val publicAuthSegments = listOf(
            "/auth/login",
            "/auth/login-google",
            "/auth/login-facebook",
            "/auth/login-social",
            "/auth/register",
            "/auth/refresh",
            "/auth/forgot-password",
            "/auth/verify-reset-otp",
            "/auth/reset-password",
            "/auth/verify",
            "/auth/reset-email",
        )
        return publicAuthSegments.none { path.contains(it) }
    }

    private fun OkHttpClient.Builder.applyDefaultTimeouts(): OkHttpClient.Builder {
        return connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            applyDefaultTimeouts()
            addInterceptor(authInterceptor)
            if (BuildConfig.DEBUG) {
                addInterceptor(loggingInterceptor)
            }
        }.build()
    }

    @Provides
    @Singleton
    @Named("PublicOkHttpClient")
    fun providePublicOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            applyDefaultTimeouts()
            if (BuildConfig.DEBUG) {
                addInterceptor(loggingInterceptor)
            }
        }.build()
    }

    @Provides
    @Singleton
    @Named("MainRetrofit")
    fun provideRetrofit(json: Json, okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @Named("PhotonRetrofit")
    fun providePhotonRetrofit(
        json: Json,
        @Named("PublicOkHttpClient") publicOkHttpClient: OkHttpClient
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(PhotonService.BASE_URL)
            .client(publicOkHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(@Named("MainRetrofit") retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCategoryApi(@Named("MainRetrofit") retrofit: Retrofit): CategoryApi {
        return retrofit.create(CategoryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRestaurantApi(@Named("MainRetrofit") retrofit: Retrofit): RestaurantApi {
        return retrofit.create(RestaurantApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApi(@Named("MainRetrofit") retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAddressApi(@Named("MainRetrofit") retrofit: Retrofit): AddressApi {
        return retrofit.create(AddressApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOrderApi(@Named("MainRetrofit") retrofit: Retrofit): OrderApi {
        return retrofit.create(OrderApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCartApi(@Named("MainRetrofit") retrofit: Retrofit): CartApi {
        return retrofit.create(CartApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChatApi(@Named("MainRetrofit") retrofit: Retrofit): ChatApi {
        return retrofit.create(ChatApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApi(@Named("MainRetrofit") retrofit: Retrofit): NotificationApi {
        return retrofit.create(NotificationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDeviceApi(@Named("MainRetrofit") retrofit: Retrofit): DeviceApi {
        return retrofit.create(DeviceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFoodApi(@Named("MainRetrofit") retrofit: Retrofit): FoodApi {
        return retrofit.create(FoodApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeApi(@Named("MainRetrofit") retrofit: Retrofit): HomeApi {
        return retrofit.create(HomeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSearchApi(@Named("MainRetrofit") retrofit: Retrofit): SearchApi {
        return retrofit.create(SearchApi::class.java)
    }

    @Provides
    @Singleton
    fun provideVoucherApi(@Named("MainRetrofit") retrofit: Retrofit): VoucherApi {
        return retrofit.create(VoucherApi::class.java)
    }

    @Provides
    @Singleton
    fun providePaymentApi(@Named("MainRetrofit") retrofit: Retrofit): PaymentApi {
        return retrofit.create(PaymentApi::class.java)
    }

    @Provides
    @Singleton
    fun providePhotonService(@Named("PhotonRetrofit") retrofit: Retrofit): PhotonService {
        return retrofit.create(PhotonService::class.java)
    }
}
