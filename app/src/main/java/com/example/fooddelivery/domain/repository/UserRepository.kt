package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.model.UserReview

interface UserRepository {
    suspend fun getUserProfile(): Result<User>
    suspend fun updateUserProfile(user: User, imageUri: String? = null): Result<User>
    suspend fun addUserPhone(phone: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getUserReviews(limit: Int = 20, offset: Int = 0): Result<List<UserReview>>
    suspend fun getFavoriteRestaurants(limit: Int = 20, offset: Int = 0): Result<List<Restaurant>>
}