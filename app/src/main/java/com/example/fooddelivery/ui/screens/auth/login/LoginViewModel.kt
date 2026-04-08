package com.example.fooddelivery.ui.screens.auth.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
class LoginViewModel @Inject constructor() : ViewModel() {
    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    fun onPhoneChange(phone: String) {
        _state.value = _state.value.copy(phone = phone, phoneError = null, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, passwordError = null, errorMessage = null)
    }

    fun onRememberMeChange(checked: Boolean) {
        _state.value = _state.value.copy(rememberMe = checked)
    }

    fun login() {
        val currentState = _state.value

        val newPhoneError = if (currentState.phone.isBlank()) "Phone number cannot be empty" else null
        val newPasswordError = if (currentState.password.isBlank()) "Password cannot be empty" else null

        if (newPhoneError != null || newPasswordError != null) {
            _state.value = currentState.copy(
                phoneError = newPhoneError,
                passwordError = newPasswordError
            )
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(
                isLoading = true,
                errorMessage = null,
                phoneError = null,
                passwordError = null
            )

            delay(2000)

            if (currentState.phone == "0867070087" &&
                currentState.password == "123") {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }
            else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Invalid phone number or password"
                )
            }
        }
    }
}