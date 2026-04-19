package com.example.fooddelivery.domain.repository

interface AuthRepository {
    suspend fun login (phone: String, password: String) : Result<String>
    suspend fun loginFacebook(facebookToken: String) : Result<String>
    suspend fun register (fullName: String, email: String, phone: String, password: String): Result<String>
}