package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.repository.ChatRepository
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(imagePath: String): Result<String> {
        return chatRepository.uploadImage(imagePath)
    }
}
