package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.UserApi
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.model.UserReview
import com.example.fooddelivery.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.local.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val tokenManager: TokenManager,
    private val database: AppDatabase
) : UserRepository {
    override suspend fun getUserProfile(): Result<User> {
        return try {
            val response = api.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Result.success(
                    User(
                        fullName = dto.name,
                        email = dto.email,
                        phone = dto.phone,
                        birthday = dto.birthday ?: "",
                        profileImage = dto.avatar
                    )
                )
            }
            else {
                Result.failure(Exception("Failed to load profile: ${response.message()}"))
            }
        } catch (e : Exception){
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            val response = api.updateUserProfile(user)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update user profile"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            try {
                api.logout()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
            // clear local data
            tokenManager.clearAuthData()
            // delete all data
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getUserReviews(limit: Int, offset: Int): Result<List<UserReview>> {
        return try {
            val response = api.getUserReviews(limit, offset)
            if (response.isSuccessful && response.body() != null) {
                val reviews = response.body()!!.map { dto ->
                    UserReview(
                        id = dto.id.toString(),
                        restaurantId = dto.restaurantId.toString(),
                        restaurantName = dto.restaurantName,
                        restaurantImage = dto.restaurantImage ?: "",
                        rating = dto.vote,
                        comment = dto.comment ?: "",
                        tags = dto.tags,
                        createdAt = try {
                            ZonedDateTime.parse(dto.createdAt).toInstant().toEpochMilli()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        },
                        orderId = dto.orderId.toString()
                    )
                }
                Result.success(reviews)
            } else {
                Result.failure(Exception("Failed to load reviews: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}