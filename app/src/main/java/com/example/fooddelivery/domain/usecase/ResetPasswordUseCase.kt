package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateUseCase: ValidateAuthInputUseCase
) {
    suspend operator fun invoke(resetToken: String, newPassword: String): Result<Unit> {
        val passwordError = validateUseCase.validatePassword(newPassword)
        if (passwordError != null) return Result.failure(Exception(passwordError))

        if (resetToken.isBlank()) return Result.failure(Exception("Reset token is missing"))

        return authRepository.resetPassword(resetToken, newPassword)
    }
}
