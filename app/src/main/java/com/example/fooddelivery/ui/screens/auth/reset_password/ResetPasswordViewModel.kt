package com.example.fooddelivery.ui.screens.auth.reset_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.ResetPasswordUseCase
import com.example.fooddelivery.domain.usecase.ValidateAuthInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetPasswordState(
    val resetToken: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",

    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)
sealed interface ResetPasswordEvent {
    data class Init(val resetToken: String): ResetPasswordEvent
    data class NewPasswordChanged(val newPassword: String): ResetPasswordEvent
    data class ConfirmPasswordChanged(val confirmPassword: String): ResetPasswordEvent
    object ResetPasswordClicked: ResetPasswordEvent
    object ErrorDismissed: ResetPasswordEvent
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val validateInputUseCase: ValidateAuthInputUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ResetPasswordState())
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()

    fun onEvent(event: ResetPasswordEvent) {
        when (event) {
            is ResetPasswordEvent.Init -> {
                _state.update { it.copy(resetToken = event.resetToken) }
            }
            is ResetPasswordEvent.NewPasswordChanged -> {
                _state.update { it.copy(newPassword = event.newPassword, passwordError = null, errorMessage = null) }
            }
            is ResetPasswordEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.confirmPassword, confirmPasswordError = null, errorMessage = null) }
            }
            ResetPasswordEvent.ResetPasswordClicked -> {
                resetPassword()
            }
            ResetPasswordEvent.ErrorDismissed -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun validateInput(): Boolean {
        val currentState = _state.value

        val passwordError = validateInputUseCase.validatePassword(currentState.newPassword)
        val confirmError = validateInputUseCase.validateConfirmPassword(currentState.newPassword, currentState.confirmPassword)

        val hasError = listOf(passwordError, confirmError).any { it != null }

        if (hasError) {
            _state.update {
                it.copy(
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
        }
        return !hasError
    }

    fun resetPassword() {
        if (_state.value.isLoading) return
        if (!validateInput()) return

        val currentState = _state.value

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = resetPasswordUseCase(
                resetToken = currentState.resetToken,
                newPassword = currentState.newPassword
            )

            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Reset password failed"
                    )
                }
            }
        }
    }
}