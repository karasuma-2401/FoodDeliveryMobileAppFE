package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.remote.socket.ChatSocketManager
import com.example.fooddelivery.data.remote.dto.MeResponse
import com.example.fooddelivery.domain.exception.UnauthorizedException
import com.example.fooddelivery.domain.exception.UserNotFoundException
import com.example.fooddelivery.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ValidateSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val chatSocketManager: ChatSocketManager
) {
    suspend operator fun invoke(): Result<MeResponse> {
        val accessToken = tokenManager.getAccessToken.first()
        if (accessToken.isNullOrBlank()) {
            return Result.failure(UnauthorizedException("Not logged in"))
        }

        return fetchMeWithRefresh()
    }

    private suspend fun fetchMeWithRefresh(): Result<MeResponse> {
        val result = authRepository.getMe()

        if (result.isSuccess) {
            val me = result.getOrThrow()
            val id = me.getFinalId()
            val email = me.getFinalEmail()
            val roles = me.getFinalRoles()
            if (id != null && !email.isNullOrBlank()) {
                tokenManager.saveMeInfo(id, email, roles)
                return Result.success(me)
            }
            return Result.failure(Exception("Invalid user profile from server"))
        }

        val error = result.exceptionOrNull()
        if (error is UnauthorizedException) {
            val refreshToken = tokenManager.getRefreshToken.first()
            if (!refreshToken.isNullOrBlank()) {
                val refreshResult = authRepository.refreshToken(refreshToken)
                refreshResult.onSuccess { response ->
                    val newAccessToken = response.getFinalAccessToken()
                    val newRefreshToken = response.getFinalRefreshToken()
                    if (!newAccessToken.isNullOrBlank() && !newRefreshToken.isNullOrBlank()) {
                        tokenManager.updateTokens(newAccessToken, newRefreshToken)
                        chatSocketManager.reconnectWithCurrentToken()
                        val retryResult = authRepository.getMe()
                        if (retryResult.isSuccess) {
                            val me = retryResult.getOrThrow()
                            val id = me.getFinalId()
                            val email = me.getFinalEmail()
                            val roles = me.getFinalRoles()
                            if (id != null && !email.isNullOrBlank()) {
                                tokenManager.saveMeInfo(id, email, roles)
                                return Result.success(me)
                            }
                        }
                    }
                }
            }
        }

        if (error is UnauthorizedException || error is UserNotFoundException) {
            tokenManager.clearAuthData()
            chatSocketManager.disconnect()
        }

        return result
    }
}
