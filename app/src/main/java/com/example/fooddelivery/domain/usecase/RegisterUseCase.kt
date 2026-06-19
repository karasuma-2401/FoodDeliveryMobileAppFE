package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(fullName: String, email: String, phone: String, password: String, agreeToTerms: Boolean): Result<Unit> {
        if (!agreeToTerms) {
            return Result.failure(Exception("You must accept the Terms of Service and Privacy Policy."))
        }
        val result = authRepository.register(
            name = fullName,
            email = email,
            phone = phone,
            password = password
        )

        return result.map { _ -> Unit }
    }
}