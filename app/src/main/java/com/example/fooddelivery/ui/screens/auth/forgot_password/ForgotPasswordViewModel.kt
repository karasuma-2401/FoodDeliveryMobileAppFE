package com.example.fooddelivery.ui.screens.auth.forgot_password

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.SendResetPasswordCodeUseCase
import com.example.fooddelivery.domain.usecase.ValidateAuthInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val successEmail: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendResetPasswordCodeUseCase: SendResetPasswordCodeUseCase,
    private val validateAuthInputUseCase: ValidateAuthInputUseCase
) : ViewModel() {

    private val _state = mutableStateOf(ForgotPasswordState())
    val state: State<ForgotPasswordState> = _state

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, emailError = null, errorMessage = null)
    }

    private fun validateInput(): Boolean {
        val emailError = validateAuthInputUseCase.validateEmail(_state.value.email)
        _state.value = _state.value.copy(emailError = emailError)
        return emailError == null
    }

    fun clearSuccessEmail() {
        _state.value = _state.value.copy(successEmail = null)
    }

    fun sendResetCode() {
        if (_state.value.isLoading) return
        if (!validateInput()) return

        val submittedEmail = _state.value.email
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = sendResetPasswordCodeUseCase(submittedEmail)
            
            result.onSuccess {
                _state.value = _state.value.copy(
                    isLoading = false, 
                    successEmail = submittedEmail
                )
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false, 
                    errorMessage = exception.message ?: "Failed to send reset code"
                )
            }
        }
    }
}