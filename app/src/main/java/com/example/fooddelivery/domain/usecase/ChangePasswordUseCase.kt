package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(currentPass: String, newPass: String): Result<Unit> {
        val userEmail = tokenManager.getUserEmail.first()
        val userPhone = tokenManager.getPhone.first()
        
        return authRepository.changePassword(
            email = userEmail,
            phone = userPhone,
            currentPass = currentPass,
            newPass = newPass
        )
    }
}