package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.User

interface UserRepository {
    suspend fun getUserProfile(): Result<User>
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun logout(): Result<Unit>
}