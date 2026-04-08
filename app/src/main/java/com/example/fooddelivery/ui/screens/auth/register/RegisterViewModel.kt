package com.example.fooddelivery.ui.screens.auth.register

import android.util.Patterns
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val agreeToTerms: Boolean = false,

    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {
    private val _state = mutableStateOf(RegisterState())

    val state: State<RegisterState> = _state
    fun onFullNameChange(fullName: String) {
        _state.value = _state.value.copy(fullName = fullName, fullNameError = null, errorMessage = null )
    }
    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email, emailError = null, errorMessage = null)
    }
    fun onPhoneChange(phone: String) {
        _state.value = _state.value.copy(phone = phone, phoneError = null, errorMessage = null)
    }
    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, passwordError = null, errorMessage = null)
    }
    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.value = _state.value.copy(confirmPassword = confirmPassword , confirmPasswordError = null, errorMessage = null)
    }
    fun onAgreeToTermsChange(value: Boolean) {
        _state.value = _state.value.copy(agreeToTerms = value)
    }
    fun register() {
        val currentState = _state.value

        val newFullNameError = if (currentState.fullName.isBlank()) "Full name cannot be empty" else null
        val newEmailError = if (!Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            "Invalid email format"
        } else null
        val newPhoneError = if (!Patterns.PHONE.matcher(currentState.phone).matches()) {
            "Invalid phone format"
        } else null
        val newPasswordError = if (currentState.password.isBlank()) "Password cannot be empty" else null
        val newConfirmPasswordError = if (currentState.confirmPassword != currentState.password) {
            "Passwords do not match"
        } else null


        if (newFullNameError != null || newEmailError != null || newPasswordError != null || newConfirmPasswordError != null) {
            _state.value = currentState.copy(
                fullNameError =  newFullNameError,
                emailError = newEmailError,
                phoneError = newPhoneError,
                passwordError = newPasswordError,
                confirmPasswordError = newConfirmPasswordError
            )
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(
                isLoading = true,
                errorMessage = null,
                fullNameError = null,
                emailError = null,
                phoneError = null,
                passwordError = null,
                confirmPasswordError = null
            )
            // simulate network request
            delay(2000)

            _state.value = _state.value.copy(isLoading = false, isSuccess = true)
        }
    }
}

