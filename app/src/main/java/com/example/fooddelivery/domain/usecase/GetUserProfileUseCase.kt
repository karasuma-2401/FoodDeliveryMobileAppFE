package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.UserRepository
import com.example.fooddelivery.domain.model.User
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<User> {
        return userRepository.getUserProfile()
    }
}