package com.example.fooddelivery.ui.screens.auth.reset_password

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetPasswordState(
    val newPassword: String = "",
    val confirmPassword: String = "",

    val passwordError: String? = null,

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String ?= null,
)

@HiltViewModel
class ResetPasswordViewModel @Inject constructor() : ViewModel() {
    private val _state = mutableStateOf(ResetPasswordState())
    val state: State<ResetPasswordState> = _state

    fun onNewPasswordChange(newPassword: String) {
        _state.value = _state.value.copy(newPassword = newPassword, passwordError = null)
    }

    fun onConfirmPasswordChange (confirmPassword: String) {
        _state.value = _state.value.copy(confirmPassword = confirmPassword, passwordError = null)
    }

    fun resetPassword() {
        val currentState = _state.value

        if (currentState.newPassword.isBlank() || currentState.confirmPassword.isBlank()) {
            _state.value = currentState.copy(passwordError = "Password cannot be empty")
            return
        }

        if (currentState.newPassword != currentState.confirmPassword) {
            _state.value = currentState.copy(passwordError = "Password does not match")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, errorMessage = null)

            try{
                // call API here
                delay(2000)
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }
            catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Please try again"
                )
            }
        }
    }
}