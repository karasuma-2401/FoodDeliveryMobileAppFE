package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.remote.socket.ChatSocketManager
import com.example.fooddelivery.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val chatSocketManager: ChatSocketManager
) {
    suspend operator fun invoke(): Result<Unit> {
        return userRepository.logout().also { result ->
            if (result.isSuccess) {
                chatSocketManager.disconnect()
            }
        }
    }
}