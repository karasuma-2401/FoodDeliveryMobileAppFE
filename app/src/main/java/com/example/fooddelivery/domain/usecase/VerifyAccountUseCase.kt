package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.remote.dto.AuthResponse
import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyAccountUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(otp: String): Result<AuthResponse> {
        return repository.verifyAccount(otp)
    }
}
