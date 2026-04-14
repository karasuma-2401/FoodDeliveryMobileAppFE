package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.AuthApi
import com.example.fooddelivery.data.remote.dto.FacebookLoginRequest
import com.example.fooddelivery.data.remote.dto.LoginRequest
import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi
) : AuthRepository {
    override suspend fun login(phone: String, password: String): Result<String> {
        return try {
            val request = LoginRequest(phone, password)
            val response = api.login(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.isSuccess == true && body.token != null) {
                    Result.success(body.token)
                }
                else {
                    Result.failure(Exception(body?.message ?: "Login failed from server"))
                }
            }
            else {
                Result.failure(Exception("Error server: ${response.code()}"))
            }
        }
        catch (e: Exception) {
            Result.failure(Exception("Cannot connect to server ! Please check out again" + e.message))
            }
        }

    override suspend fun loginFacebook(facebookToken: String): Result<String> {
        return try {
            val response = api.loginFacebook(FacebookLoginRequest(facebookToken))
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                val token = response.body()?.token
                if (token != null) {
                    Result.success(token)
                }
                else {
                    Result.failure(Exception("Server response success without token"))
                }
            }
            else {
                Result.failure(Exception(response.body()?.message?: "Login with facebook failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
