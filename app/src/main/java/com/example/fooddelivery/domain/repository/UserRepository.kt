package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.model.UserReview

interface UserRepository {
    suspend fun getUserProfile(): Result<User>
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun getUserReviews(limit: Int = 20, offset: Int = 0): Result<List<UserReview>>
}