package com.example.fooddelivery.ui.screens.auth.register

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.LoginWithFacebookUseCase
import com.example.fooddelivery.domain.usecase.RegisterUseCase
import com.example.fooddelivery.domain.usecase.ValidateAuthInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val isFacebookAuthSuccess: Boolean = false
)
sealed interface RegisterEvent {
    data class FullNameChanged(val fullName: String): RegisterEvent
    data class EmailChanged(val email: String): RegisterEvent
    data class PhoneChanged(val phone: String): RegisterEvent
    data class PasswordChanged(val password: String): RegisterEvent
    data class ConfirmPasswordChanged(val confirmPassword: String): RegisterEvent
    data class AgreeToTermsChanged(val agreeToTerms: Boolean): RegisterEvent
    data class FacebookLoginClicked(val token: String): RegisterEvent
    data class ErrorMessageSet(val message: String): RegisterEvent
    object RegisterClicked: RegisterEvent
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val loginWithFacebookUseCase: LoginWithFacebookUseCase,
    private val validateInputUseCase: ValidateAuthInputUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()
    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.FullNameChanged -> {
                _state.update { it.copy(fullName = event.fullName, fullNameError = null, errorMessage = null) }
            }
            is RegisterEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, emailError = null, errorMessage = null) }
            }
            is RegisterEvent.PhoneChanged -> {
                _state.update { it.copy(phone = event.phone, phoneError = null, errorMessage = null) }
            }
            is RegisterEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, passwordError = null, errorMessage = null) }
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.confirmPassword, confirmPasswordError = null, errorMessage = null) }
            }
            is RegisterEvent.AgreeToTermsChanged -> {
                _state.update { it.copy(agreeToTerms = event.agreeToTerms, errorMessage = null) }
            }
            is RegisterEvent.FacebookLoginClicked -> {
                loginWithFacebook(event.token)
            }
            is RegisterEvent.ErrorMessageSet -> {
                setErrorMessage(event.message)
            }
            RegisterEvent.RegisterClicked -> {
                register()
            }
        }
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
            _state.update {
                it.copy(
                    fullNameError = fullNameError,
                    emailError = emailError,
                    phoneError = phoneError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
        }
        return !hasError
    }

    private fun setErrorMessage (message: String) {
        _state.update { it.copy(errorMessage = message) }
    }

    private fun loginWithFacebook(facebookToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = loginWithFacebookUseCase(facebookToken)
            result.onSuccess {
                _state.update { it.copy(isLoading = false, isFacebookAuthSuccess = true) }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login with Facebook failed"
                    )
                }
            }
        }
    }

    private fun register() {
        if (!validateInput()) return
        val currentState = _state.value

        if (!currentState.agreeToTerms) {
            _state.update { it.copy(errorMessage = "You must accept the Terms of Service and Privacy Policy.") }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }
            val result = registerUseCase(
                fullName = currentState.fullName,
                email = currentState.email,
                phone = currentState.phone,
                password = currentState.password,
                agreeToTerms = currentState.agreeToTerms
            )
            
            result.onSuccess {
                _state.update { it.copy(isLoading =  false, isSuccess =  true) }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Registration failed. Please try again."
                    )
                }
            }
        }
    }
}