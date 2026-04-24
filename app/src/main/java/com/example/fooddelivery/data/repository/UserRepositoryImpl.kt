package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.UserApi
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {
    override suspend fun getUserProfile(): Result<User> {
        return try {
            val response = api.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
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
}