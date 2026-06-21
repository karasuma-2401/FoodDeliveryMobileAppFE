package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val validateUseCase: ValidateAuthInputUseCase
) {
    suspend operator fun invoke(currentPass: String, newPass: String): Result<Unit> {
        // 1. Validate Input
        val currentPassError = validateUseCase.validatePassword(currentPass)
        if (currentPassError != null) return Result.failure(Exception(currentPassError))

        val newPassError = validateUseCase.validateNewPassword(currentPass, newPass)
        if (newPassError != null) return Result.failure(Exception(newPassError))

        // 2. Lấy thông tin user từ TokenManager
        val userEmail = tokenManager.getUserEmail.first()
        val userPhone = tokenManager.getPhone.first()

        if (userEmail.isNullOrBlank() && userPhone.isNullOrBlank()) {
            return Result.failure(Exception("User email or phone is required to change password"))
        }

        // 3. Gọi Repository
        return authRepository.changePassword(
            email = userEmail,
            phone = userPhone,
            currentPass = currentPass,
            newPass = newPass
        )
    }
}
