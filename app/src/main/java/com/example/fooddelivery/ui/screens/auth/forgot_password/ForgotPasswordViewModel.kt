package com.example.fooddelivery.ui.screens.auth.forgot_password

import android.util.Patterns
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordState(
    val email: String = "",
    val emailError: String? = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(ForgotPasswordState())
    val state: State<ForgotPasswordState> = _state

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, emailError = null)
    }

    fun sendResetCode() {
        val currentState = _state.value
        val email = currentState.email

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = currentState.copy(emailError = "Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, errorMessage = null)
            delay(2000)

            _state.value = _state.value.copy(isLoading = false, isSuccess = true)
        }
    }
}