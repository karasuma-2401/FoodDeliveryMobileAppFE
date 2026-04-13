package com.example.fooddelivery.domain.repository

interface AuthRepository {
    suspend fun login (phone: String, password: String) : Result<String>
}