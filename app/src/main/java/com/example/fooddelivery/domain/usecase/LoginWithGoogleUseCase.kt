package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.remote.socket.ChatSocketManager
import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val chatSocketManager: ChatSocketManager
) {
    suspend operator fun invoke(accessToken: String? = null, code: String? = null): Result<List<String>> {
        val result = authRepository.loginGoogle(accessToken = accessToken, code = code)

        return result.mapCatching { response ->
            val user = response.getFinalUser() ?: throw Exception("User data missing in response")
            val finalAccessToken = response.getFinalAccessToken() ?: throw Exception("Access token missing in response")
            val finalRefreshToken = response.getFinalRefreshToken() ?: throw Exception("Refresh token missing in response")

            tokenManager.saveAuthData(
                accessToken = finalAccessToken,
                refreshToken = finalRefreshToken,
                phone = user.phone ?: "",
                name = user.name,
                email = user.email,
                rememberMe = true
            )
            tokenManager.saveMeInfo(user.id, user.email, user.roles)
            chatSocketManager.reconnectWithCurrentToken()
            user.roles
        }
    }
}
