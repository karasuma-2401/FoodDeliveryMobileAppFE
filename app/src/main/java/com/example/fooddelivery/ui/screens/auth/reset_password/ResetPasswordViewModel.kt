package com.example.fooddelivery.ui.screens.auth.reset_password

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.ResetPasswordUseCase
import com.example.fooddelivery.domain.usecase.ValidateAuthInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetPasswordState(
    val email: String = "",
    val resetCode: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",

    val resetCodeError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val validateInputUseCase: ValidateAuthInputUseCase
) : ViewModel() {
    private val _state = mutableStateOf(ResetPasswordState())
    val state: State<ResetPasswordState> = _state

    fun setEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun onResetCodeChange(code: String) {
        _state.value = _state.value.copy(resetCode = code, resetCodeError = null)
    }

    fun onNewPasswordChange(newPassword: String) {
        _state.value = _state.value.copy(newPassword = newPassword, passwordError = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.value = _state.value.copy(confirmPassword = confirmPassword, confirmPasswordError = null)
    }

    private fun validateInput(): Boolean {
        val currentState = _state.value

        val codeError = if (currentState.resetCode.isBlank()) "Reset code cannot be empty" else null
        val passwordError = validateInputUseCase.validatePassword(currentState.newPassword)
        val confirmError = validateInputUseCase.validateConfirmPassword(currentState.newPassword, currentState.confirmPassword)

        val hasError = listOf(codeError, passwordError, confirmError).any { it != null }

        if (hasError) {
            _state.value = currentState.copy(
                resetCodeError = codeError,
                passwordError = passwordError,
                confirmPasswordError = confirmError
            )
        }
        return !hasError
    }

    fun resetPassword() {
        if (_state.value.isLoading) return
        if (!validateInput()) return

        val currentState = _state.value

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, errorMessage = null)

            val result = resetPasswordUseCase(
                email = currentState.email,
                resetCode = currentState.resetCode,
                newPass = currentState.newPassword
            )

            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Reset password failed"
                )
            }
        }
    }
}