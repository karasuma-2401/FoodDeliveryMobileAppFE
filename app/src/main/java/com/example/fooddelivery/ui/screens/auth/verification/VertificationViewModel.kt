package com.example.fooddelivery.ui.screens.auth.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.fooddelivery.domain.usecase.VerifyCodeUseCase

data class VerificationState(
    val email: String = "",
    val otpCode: String = "",
    val timeLeft: Int = 50,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
sealed interface VerificationEvent {
    data class Init(val email: String): VerificationEvent
    data class OtpChanged(val code: String): VerificationEvent
    object ResendCodeClicked: VerificationEvent
    object VerifyClicked: VerificationEvent
    object ErrorDismissed: VerificationEvent
}

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verifyCodeUseCase: VerifyCodeUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(VerificationState())
    val state: StateFlow<VerificationState> = _state.asStateFlow()

    private var timerJob: Job? = null
    fun onEvent(event: VerificationEvent) {
        when (event) {
            is VerificationEvent.Init -> {
                if (_state.value.email.isBlank()) {
                    _state.update { it.copy(email = event.email) }
                    startTimer()
                }
            }
            is VerificationEvent.OtpChanged -> {
                _state.update { it.copy(otpCode = event.code, errorMessage = null) }
            }
            VerificationEvent.ResendCodeClicked -> {
                if (_state.value.timeLeft == 0) {
                    startTimer()
                }
            }
            VerificationEvent.VerifyClicked -> {
                verifyCode()
            }
            VerificationEvent.ErrorDismissed -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _state.update { it.copy(timeLeft = 60) }
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeft > 0) {
                delay(1000L)
                _state.update { it.copy(timeLeft = _state.value.timeLeft - 1) }
            }
        }
    }
    private fun verifyCode() {

        if (_state.value.otpCode.length < 4) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = verifyCodeUseCase(
                email = _state.value.email,
                code = _state.value.otpCode
            )
            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Invalid otp"
                    )
                }
            }
        }
    }
}