package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(fullName: String, email: String, phone: String, password: String, agreeToTerms: Boolean): Result<Unit> {
        if (!agreeToTerms) {
            return Result.failure(Exception("You must accept the Terms of Service and Privacy Policy."))
        }
        val result = authRepository.register(fullName, email, phone, password)

        return result.mapCatching { token ->
            tokenManager.saveAuthData(
                token = token,
                phone = phone,
                rememberMe = true
            )
        }
    }
}