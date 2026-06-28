package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.UserRepository
import javax.inject.Inject

class AddUserPhoneUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val validateInputUseCase: ValidateAuthInputUseCase,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        val phoneError = validateInputUseCase.validatePhone(phone)
        if (phoneError != null) {
            return Result.failure(Exception(phoneError))
        }

        val profileResult = userRepository.getUserProfile()
        val currentUser = profileResult.getOrElse { return Result.failure(it) }
        if (currentUser.phone.isNotBlank()) {
            return Result.failure(Exception("Phone number is already set"))
        }

        return userRepository.addUserPhone(phone).mapCatching { updatedUser ->
            tokenManager.savePhone(updatedUser.phone)
        }
    }
}
