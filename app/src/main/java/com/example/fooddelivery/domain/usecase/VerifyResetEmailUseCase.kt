package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyResetEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(newEmail: String, otp: String): Result<Unit> {
        return repository.verifyResetEmail(newEmail, otp)
    }
}