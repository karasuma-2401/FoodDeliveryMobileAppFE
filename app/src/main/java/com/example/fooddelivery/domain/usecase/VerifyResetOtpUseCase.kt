package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.remote.dto.VerifyResetOtpResponse
import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyResetOtpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String): Result<VerifyResetOtpResponse> {
        return repository.verifyResetOtp(email, otp)
    }
}