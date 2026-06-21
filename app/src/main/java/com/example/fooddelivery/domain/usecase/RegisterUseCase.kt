package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.data.remote.dto.RegisterResponse
import com.example.fooddelivery.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        fullName: String, 
        email: String, 
        phone: String, 
        password: String, 
        agreeToTerms: Boolean,
        birthday: String? = null
    ): Result<RegisterResponse> {
        if (!agreeToTerms) {
            return Result.failure(Exception("You must accept the Terms of Service and Privacy Policy."))
        }
        
        return authRepository.register(
            name = fullName,
            email = email,
            phone = phone,
            password = password,
            birthday = birthday
        )
    }
}
