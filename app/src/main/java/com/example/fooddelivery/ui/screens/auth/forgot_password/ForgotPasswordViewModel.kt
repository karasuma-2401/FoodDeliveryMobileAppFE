package com.example.fooddelivery.ui.screens.auth.forgot_password

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.SendResetPasswordCodeUseCase
import com.example.fooddelivery.domain.usecase.ValidateAuthInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val successEmail: String? = null,
    val errorMessage: String? = null
)
sealed interface ForgotPasswordEvent {
    data class EmailChanged(val email: String): ForgotPasswordEvent
    data class ErrorMessageSet(val message: String): ForgotPasswordEvent
    object ResetSuccessState: ForgotPasswordEvent
    object SendResetCodeClicked: ForgotPasswordEvent
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendResetPasswordCodeUseCase: SendResetPasswordCodeUseCase,
    private val validateAuthInputUseCase: ValidateAuthInputUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEvent (event: ForgotPasswordEvent) {
        when(event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, emailError = null, errorMessage = null) }
            }
            is ForgotPasswordEvent.ErrorMessageSet -> {
                _state.update { it.copy(errorMessage = event.message) }
            }
            ForgotPasswordEvent.SendResetCodeClicked -> {
                sendResetCode()
            }
            ForgotPasswordEvent.ResetSuccessState -> {
                _state.update { it.copy(successEmail = null) }
            }

        }
    }

    private fun validateInput(): Boolean {
        val emailError = validateAuthInputUseCase.validateEmail(_state.value.email)
        _state.update { it.copy(emailError = emailError) }
        return emailError == null
    }

    private fun sendResetCode() {
        if (_state.value.isLoading) return
        if (!validateInput()) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val submittedEmail = _state.value.email
            val result = sendResetPasswordCodeUseCase(submittedEmail)
            
            result.onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        successEmail = submittedEmail
                    )
                }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to send reset code"
                    )
                }
            }
        }
    }
}