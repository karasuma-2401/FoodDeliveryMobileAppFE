package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.remote.dto.AuthResponse
import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String): Result<Unit> {
        return repository.verifyCode(email, code)
    }
}