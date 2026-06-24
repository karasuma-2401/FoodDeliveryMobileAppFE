package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.remote.dto.MeResponse
import com.example.fooddelivery.domain.exception.UnauthorizedException
import com.example.fooddelivery.domain.exception.UserNotFoundException
import com.example.fooddelivery.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ValidateSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): Result<MeResponse> {
        val accessToken = tokenManager.getAccessToken.first()
        if (accessToken.isNullOrBlank()) {
            return Result.failure(UnauthorizedException("Chưa đăng nhập"))
        }

        return fetchMeWithRefresh()
    }

    private suspend fun fetchMeWithRefresh(): Result<MeResponse> {
        val result = authRepository.getMe()

        if (result.isSuccess) {
            val me = result.getOrThrow()
            tokenManager.saveMeInfo(me.id, me.email, me.roles)
            return Result.success(me)
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
                        val retryResult = authRepository.getMe()
                        if (retryResult.isSuccess) {
                            val me = retryResult.getOrThrow()
                            tokenManager.saveMeInfo(me.id, me.email, me.roles)
                            return Result.success(me)
                        }
                    }
                }
            }
        }

        if (error is UnauthorizedException || error is UserNotFoundException) {
            tokenManager.clearAuthData()
        }

        return result
    }
}
