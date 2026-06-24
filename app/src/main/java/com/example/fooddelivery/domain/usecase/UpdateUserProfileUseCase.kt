package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User, imageUri: String? = null): Result<User> {
        return userRepository.updateUserProfile(user, imageUri)
    }
}
