package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateUseCase: ValidateAuthInputUseCase
) {
    suspend operator fun invoke(email: String, otp: String, newPassword: String): Result<Unit> {
        val emailError = validateUseCase.validateEmail(email)
        if (emailError != null) return Result.failure(Exception(emailError))

        val otpError = validateUseCase.validateOtp(otp)
        if (otpError != null) return Result.failure(Exception(otpError))

        val passwordError = validateUseCase.validatePassword(newPassword)
        if (passwordError != null) return Result.failure(Exception(passwordError))

        return authRepository.resetPassword(email, otp, newPassword)
    }
}
