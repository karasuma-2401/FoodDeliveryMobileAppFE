package com.example.fooddelivery.ui.screens.auth.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.SendResetPasswordCodeUseCase
import com.example.fooddelivery.domain.usecase.VerifyAccountUseCase
import com.example.fooddelivery.domain.usecase.VerifyResetOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerificationState(
    val email: String = "",
    val otpCode: String = "",
    val timeLeft: Int = 900,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isFromRegistration: Boolean = false,
    val resetToken: String? = null,
    val errorMessage: String? = null,
    val resendMessage: String? = null
)

sealed interface VerificationEvent {
    data class Init(val email: String, val isFromRegistration: Boolean): VerificationEvent
    data class OtpChanged(val code: String): VerificationEvent
    object ResendCodeClicked: VerificationEvent
    object VerifyClicked: VerificationEvent
    object ErrorDismissed: VerificationEvent
}

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verifyResetOtpUseCase: VerifyResetOtpUseCase,
    private val verifyAccountUseCase: VerifyAccountUseCase,
    private val sendResetPasswordCodeUseCase: SendResetPasswordCodeUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(VerificationState())
    val state: StateFlow<VerificationState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun onEvent(event: VerificationEvent) {
        when (event) {
            is VerificationEvent.Init -> {
                if (_state.value.email.isBlank()) {
                    _state.update { 
                        it.copy(
                            email = event.email, 
                            isFromRegistration = event.isFromRegistration 
                        ) 
                    }
                    if (!event.isFromRegistration) {
                        startTimer()
                    }
                }
            }
            is VerificationEvent.OtpChanged -> {
                if (event.code.length <= 6) {
                    _state.update { it.copy(otpCode = event.code, errorMessage = null) }
                }
            }
            VerificationEvent.ResendCodeClicked -> {
                if (_state.value.timeLeft == 0 && !_state.value.isFromRegistration) {
                    resendCode()
                }
            }
            VerificationEvent.VerifyClicked -> {
                verifyCode()
            }
            VerificationEvent.ErrorDismissed -> {
                _state.update { it.copy(errorMessage = null, resendMessage = null) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _state.update { it.copy(timeLeft = 900) }
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeft > 0) {
                delay(1000L)
                _state.update { it.copy(timeLeft = it.timeLeft - 1) }
            }
        }
    }

    private fun resendCode() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = sendResetPasswordCodeUseCase(_state.value.email)
            
            result.onSuccess {
                _state.update { it.copy(isLoading = false, resendMessage = "Code resent successfully!") }
                startTimer()
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to resend code"
                    )
                }
            }
        }
    }

    private fun verifyCode() {
        val currentState = _state.value
        if (currentState.otpCode.length < 6) {
            _state.update { it.copy(errorMessage = "Please enter 6-digit code") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            if (currentState.isFromRegistration) {
                val result = verifyAccountUseCase(currentState.otpCode)
                result.onSuccess {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }.onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Invalid OTP"
                        )
                    }
                }
            } else {
                val result = verifyResetOtpUseCase(
                    email = currentState.email,
                    otp = currentState.otpCode
                )
                result.onSuccess { response ->
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            isSuccess = true, 
                            resetToken = response.resetToken 
                        ) 
                    }
                }.onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Invalid OTP"
                        )
                    }
                }
            }
        }
    }
}
