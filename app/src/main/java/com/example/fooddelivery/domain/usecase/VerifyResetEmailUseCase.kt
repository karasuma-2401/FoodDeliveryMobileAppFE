package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyResetEmailUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val validateUseCase: ValidateAuthInputUseCase
) {
    suspend operator fun invoke(newEmail: String, otp: String): Result<Unit> {
        // 1. Validate Email mới
        val emailError = validateUseCase.validateEmail(newEmail)
        if (emailError != null) return Result.failure(Exception(emailError))

        // 2. Validate OTP (6 chữ số)
        val otpError = validateUseCase.validateOtp(otp)
        if (otpError != null) return Result.failure(Exception(otpError))

        // 3. Gọi Repository
        return repository.verifyResetEmail(newEmail, otp)
    }
}
