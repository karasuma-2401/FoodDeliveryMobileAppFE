package com.example.fooddelivery.ui.screens.auth.register

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.LoginWithFacebookUseCase
import com.example.fooddelivery.domain.usecase.RegisterUseCase
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
    val isFacebookAuthSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val loginWithFacebookUseCase: LoginWithFacebookUseCase,
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

    fun setErrorMessage (message: String) {
        _state.value = _state.value.copy(errorMessage = message)
    }

    fun loginWithFacebook(facebookToken: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val result = loginWithFacebookUseCase(facebookToken)
            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false, isFacebookAuthSuccess = true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Login with Facebook failed"
                )
            }
        }
    }

    fun register() {
        if (!validateInput()) return
        val currentState = _state.value

        if (!currentState.agreeToTerms) {
            _state.value = currentState.copy(errorMessage = "You must accept the Terms of Service and Privacy Policy.")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(
                isLoading = true,
                errorMessage = null,
            )
            
            val result = registerUseCase(
                fullName = currentState.fullName,
                email = currentState.email,
                phone = currentState.phone,
                password = currentState.password,
                agreeToTerms = currentState.agreeToTerms
            )
            
            result.onSuccess {
                _state.value = _state.value.copy(isLoading =  false, isSuccess =  true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Registration failed. Please try again."
                )
            }
        }
    }
}