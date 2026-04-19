package com.example.fooddelivery.ui.screens.auth.register

import android.util.Patterns
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.AuthRepository
import com.example.fooddelivery.domain.usecase.ValidateAuthInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val validateInputUseCase: ValidateAuthInputUseCase
) : ViewModel() {
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

    private fun validateInput(): Boolean {
        val state = _state.value

        val fullNameError = validateInputUseCase.validateFullName(state.fullName)
        val emailError = validateInputUseCase.validateEmail(state.email)
        val phoneError = validateInputUseCase.validatePhone(state.phone)
        val passwordError = validateInputUseCase.validatePassword(state.password)
        val confirmPasswordError = validateInputUseCase.validateConfirmPassword(state.password, state.confirmPassword)

        val hasError = listOf(
            fullNameError, emailError, phoneError, passwordError, confirmPasswordError
        ).any { it != null }

        if (hasError) {
            _state.value = state.copy(
                fullNameError = fullNameError,
                emailError = emailError,
                phoneError = phoneError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }
        return !hasError
    }

    fun register() {
        if (!validateInput()) return
        val currentState = _state.value

        viewModelScope.launch {
            _state.value = currentState.copy(
                isLoading = true,
                errorMessage = null,
            )
            val result = authRepository.register(currentState.fullName, currentState.email, currentState.phone, currentState.password)
            result.onSuccess { token ->
                tokenManager.saveAuthData(
                    token = token,
                    phone = currentState.phone,
                    rememberMe = true
                )
                _state.value = _state.value.copy(isLoading =  false, isSuccess =  true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }
}

