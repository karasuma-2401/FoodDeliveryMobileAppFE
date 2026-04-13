package com.example.fooddelivery.ui.screens.auth.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState (
    val phone: String = "",
    val phoneError: String? = "",
    val password: String = "",
    val passwordError: String? ="",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository : AuthRepository
) : ViewModel() {
    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    fun onPhoneChange(phone: String) {
        if (phone.all { it.isDigit() }) {
            _state.value = _state.value.copy(phone = phone, phoneError = null, errorMessage = null)
        }
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, passwordError = null, errorMessage = null)
    }

    fun onRememberMeChange(checked: Boolean) {
        _state.value = _state.value.copy(rememberMe = checked)
    }

    private fun validateInput() : Boolean {
        val currentState = _state.value
        var isValid = true
        var phoneError: String? = null
        var passwordError: String? = null

        val phoneRegex = Regex("^(0)(3|5|7|8|9)([0-9]{8})\$")
        if (currentState.phone.isBlank()) {
            phoneError = "Phone number cannot be empty"
            isValid = false
        }
        else if (!currentState.phone.matches(phoneRegex)) {
            phoneError = "Invalid phone number format"
            isValid = false
        }

        if (currentState.password.isBlank()) {
            passwordError = "Password cannot be empty"
            isValid = false
        }
        else if (currentState.password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        }

        if (!isValid) {
            _state.value = currentState.copy(
                phoneError = phoneError,
                passwordError = passwordError
            )
        }
        return  isValid
    }

    fun login() {
        if (!validateInput()) return
        val currentState = _state.value

        viewModelScope.launch {
            _state.value = currentState.copy(
                isLoading = true,
                errorMessage = null,
            )

            val result = authRepository.login(currentState.phone, currentState.password)
            result.onSuccess { token ->
                if (currentState.rememberMe) {
//                    saveTokenLocally(token)
                }

                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading =  false,
                    errorMessage = exception.message
                )
            }
        }
    }
}