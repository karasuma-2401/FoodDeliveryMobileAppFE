package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(phone: String, password: String, rememberMe: Boolean): Result<Unit> {
        val result = authRepository.login(phone, password)
        
        return result.mapCatching { token ->
            tokenManager.saveAuthData(
                token = token,
                phone = phone,
                rememberMe = rememberMe
            )
        }
    }
}