package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, resetCode: String, newPass: String): Result<Unit> {
        return authRepository.resetPassword(email, resetCode, newPass)
    }
}