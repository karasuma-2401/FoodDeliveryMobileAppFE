package com.example.fooddelivery.domain.usecase

import javax.inject.Inject

class ValidateAuthInputUseCase @Inject constructor() {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    private val phoneRegex = Regex("^(0)[35789]([0-9]{8})$")

    fun validateFullName(fullName: String): String? {
        return if (fullName.isBlank()) "Full name cannot be empty" else null
    }

    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email cannot be empty"
            !email.matches(emailRegex) -> "Invalid email format"
            else -> null
        }
    }

    fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "Phone number cannot be empty"
            !phone.matches(phoneRegex) -> "Invalid phone number format"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password cannot be empty"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirm: String): String? {
        return when {
            confirm.isBlank() -> "Confirm Password cannot be empty"
            confirm != password -> "Confirm password does not match"
            else -> null
        }
    }
}