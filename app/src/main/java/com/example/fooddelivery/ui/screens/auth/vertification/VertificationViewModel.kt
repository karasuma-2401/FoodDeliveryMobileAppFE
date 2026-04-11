package com.example.fooddelivery.ui.screens.auth.verification

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerificationState(
    val email: String = "",
    val otpCode: String = "",
    val timeLeft: Int = 50,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class VerificationViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(VerificationState())
    val state: State<VerificationState> = _state

    private var timerJob: Job? = null
    fun initData(email: String) {
        if (_state.value.email.isBlank()) {
            _state.value = _state.value.copy(email = email)
            startTimer()
        }
    }
    fun onOtpChange(code: String) {
        if (code.length <= 4 && code.all { it.isDigit() }) {
            _state.value = _state.value.copy(otpCode = code, errorMessage = null)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _state.value = _state.value.copy(timeLeft = 60)
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeft > 0) {
                delay(1000L)
                _state.value = _state.value.copy(timeLeft = _state.value.timeLeft - 1)
            }
        }
    }
    fun resendCode() {
        if (_state.value.timeLeft == 0) {
            startTimer()
        }
    }
    fun verifyCode() {
        val currentState = _state.value

        if (currentState.otpCode.length < 4) return

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, errorMessage = null)

            delay(1500)
            if (_state.value.otpCode == "1234") {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Invalid code."
                )
            }
        }
    }
}