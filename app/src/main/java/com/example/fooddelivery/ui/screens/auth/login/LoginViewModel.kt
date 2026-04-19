package com.example.fooddelivery.ui.screens.auth.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.AuthRepository
import com.example.fooddelivery.domain.usecase.ValidateAuthInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState (
    val phone: String = "",
    val phoneError: String? = "",
    val password: String = "",
    val passwordError: String? ="",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository : AuthRepository,
    private val tokenManager: TokenManager,
    private val validateInputUseCase: ValidateAuthInputUseCase
) : ViewModel() {
    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    fun onPhoneChange(phone: String) {
        if (phone.all { it.isDigit() }) {
            _state.value = _state.value.copy(phone = phone, phoneError = null, errorMessage = null)
        }
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, passwordError = null, errorMessage = null)
    }

    fun onRememberMeChange(checked: Boolean) {
        _state.value = _state.value.copy(rememberMe = checked)
    }


    private fun validateInput() : Boolean {
        val currentState = _state.value
        val phoneError = validateInputUseCase.validatePhone(currentState.phone)
        val passwordError = validateInputUseCase.validatePassword(currentState.password)

        val hasError = listOf(phoneError, passwordError).any { it != null}
        if (hasError) {
            _state.value = currentState.copy(
                phoneError = phoneError,
                passwordError = passwordError
            )
        }
        return !hasError
    }
    fun setErrorMessage (message: String) {
        _state.value = _state.value.copy(errorMessage = message)
    }

    fun loginWithFacebook(facebookToken: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.loginFacebook(facebookToken)
            result.onSuccess { token ->
                tokenManager.saveAuthData(
                    token = token,
                    phone = "",
                    rememberMe = true
                )
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Error before connect to Server"
                )
            }
        }
    }

    fun login() {
        if (!validateInput()) return
        val currentState = _state.value

        viewModelScope.launch {
            _state.value = currentState.copy(
                isLoading = true,
                errorMessage = null,
            )

            val result = authRepository.login(currentState.phone, currentState.password)
            result.onSuccess { token ->
                tokenManager.saveAuthData(
                    token = token,
                    phone = currentState.phone,
                    rememberMe = currentState.rememberMe
                )

                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading =  false,
                    errorMessage = exception.message
                )
            }
        }
    }
}