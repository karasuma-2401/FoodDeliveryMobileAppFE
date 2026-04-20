package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class SendResetPasswordCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.sendResetPasswordCode(email)
    }
}