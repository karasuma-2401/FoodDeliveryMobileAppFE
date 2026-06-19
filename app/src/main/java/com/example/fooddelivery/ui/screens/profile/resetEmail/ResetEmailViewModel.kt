package com.example.fooddelivery.ui.screens.profile.resetEmail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.RequestResetEmailUseCase
import com.example.fooddelivery.domain.usecase.ValidateAuthInputUseCase
import com.example.fooddelivery.domain.usecase.VerifyResetEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetEmailState(
    val phone: String = "",
    val password: String = "",
    val newEmail: String = "",
    val otpCode: String = "",
    
    val phoneError: String? = null,
    val passwordError: String? = null,
    val newEmailError: String? = null,
    val otpError: String? = null,
    
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ResetEmailEvent {
    data class PhoneChanged(val value: String) : ResetEmailEvent
    data class PasswordChanged(val value: String) : ResetEmailEvent
    data class NewEmailChanged(val value: String) : ResetEmailEvent
    data class OtpChanged(val value: String) : ResetEmailEvent
    object RequestOtpClicked : ResetEmailEvent
    object VerifyOtpClicked : ResetEmailEvent
    object ErrorDismissed : ResetEmailEvent
}

@HiltViewModel
class ResetEmailViewModel @Inject constructor(
    private val requestResetEmailUseCase: RequestResetEmailUseCase,
    private val verifyResetEmailUseCase: VerifyResetEmailUseCase,
    private val validateInputUseCase: ValidateAuthInputUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ResetEmailState())
    val state: StateFlow<ResetEmailState> = _state.asStateFlow()

    fun onEvent(event: ResetEmailEvent) {
        when (event) {
            is ResetEmailEvent.PhoneChanged -> _state.update { it.copy(phone = event.value, phoneError = null) }
            is ResetEmailEvent.PasswordChanged -> _state.update { it.copy(password = event.value, passwordError = null) }
            is ResetEmailEvent.NewEmailChanged -> _state.update { it.copy(newEmail = event.value, newEmailError = null) }
            is ResetEmailEvent.OtpChanged -> _state.update { it.copy(otpCode = event.value, otpError = null) }
            ResetEmailEvent.RequestOtpClicked -> requestOtp()
            ResetEmailEvent.VerifyOtpClicked -> verifyOtp()
            ResetEmailEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun requestOtp() {
        val phoneError = validateInputUseCase.validatePhone(_state.value.phone)
        val passwordError = if (_state.value.password.isBlank()) "Password is required" else null
        
        if (phoneError != null || passwordError != null) {
            _state.update { it.copy(phoneError = phoneError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = requestResetEmailUseCase(_state.value.phone, _state.value.password)
            result.onSuccess {
                _state.update { it.copy(isLoading = false, isOtpSent = true) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to send OTP") }
            }
        }
    }

    private fun verifyOtp() {
        val emailError = validateInputUseCase.validateEmail(_state.value.newEmail)
        val otpError = when {
            _state.value.otpCode.length != 6 -> "Enter 6-digit OTP"
            !_state.value.otpCode.all { it.isDigit() } -> "OTP must contain only digits"
            else -> null
        }

        if (emailError != null || otpError != null) {
            _state.update { it.copy(newEmailError = emailError, otpError = otpError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = verifyResetEmailUseCase(_state.value.newEmail, _state.value.otpCode)
            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to verify OTP") }
            }
        }
    }
}
