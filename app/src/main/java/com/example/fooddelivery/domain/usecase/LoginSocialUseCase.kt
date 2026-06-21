package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class LoginSocialUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(
        provider: String,
        accessToken: String? = null,
        code: String? = null
    ): Result<Unit> {
        val result = authRepository.loginSocial(
            provider = provider,
            accessToken = accessToken,
            code = code
        )

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
        }
    }
}
