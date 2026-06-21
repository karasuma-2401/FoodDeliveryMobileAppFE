package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class RequestResetEmailUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val validateUseCase: ValidateAuthInputUseCase
) {
    suspend operator fun invoke(phone: String, password: String): Result<String?> {
        val phoneError = validateUseCase.validatePhone(phone)
        if (phoneError != null) return Result.failure(Exception(phoneError))

        val passwordError = validateUseCase.validatePassword(password)
        if (passwordError != null) return Result.failure(Exception(passwordError))

        return repository.requestResetEmail(phone, password)
    }
}
