package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithFacebookUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(facebookToken: String): Result<Unit> {
        val result = authRepository.loginFacebook(facebookToken)

        return result.mapCatching { token ->
            tokenManager.saveAuthData(
                token = token,
                phone = "",
                rememberMe = true
            )
        }
    }
}