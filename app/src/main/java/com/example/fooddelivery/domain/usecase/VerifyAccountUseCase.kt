package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyAccountUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(otp: String): Result<Unit> {
        return repository.verifyAccount(otp)
    }
}