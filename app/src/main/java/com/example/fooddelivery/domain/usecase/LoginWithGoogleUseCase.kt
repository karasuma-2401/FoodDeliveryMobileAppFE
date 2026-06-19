package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(googleToken: String): Result<Unit> {
        val result = authRepository.loginGoogle(googleToken)

        return result.mapCatching { response ->
            val user = response.getFinalUser() ?: throw Exception("User data missing in response")

            tokenManager.saveAuthData(
                accessToken = response.getFinalAccessToken(),
                refreshToken = response.getFinalRefreshToken(),
                phone = user.phone ?: "",
                name = user.name,
                email = user.email,
                rememberMe = true
            )
        }
    }
}
