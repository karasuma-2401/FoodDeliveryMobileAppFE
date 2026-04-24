package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return userRepository.logout()
    }
}