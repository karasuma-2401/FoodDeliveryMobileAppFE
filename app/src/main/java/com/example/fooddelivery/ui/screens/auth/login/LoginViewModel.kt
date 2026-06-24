package com.example.fooddelivery.ui.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.LoginUseCase
import com.example.fooddelivery.domain.usecase.LoginWithFacebookUseCase
import com.example.fooddelivery.domain.usecase.LoginWithGoogleUseCase
import com.example.fooddelivery.domain.usecase.ValidateAuthInputUseCase
import com.example.fooddelivery.ui.navigation.resolveStartDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState (
    val phone: String = "",
    val phoneError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val postLoginDestination: Any? = null,
)
sealed interface LoginEvent {
    data class PhoneChanged (val phone: String): LoginEvent
    data class PasswordChanged(val password: String): LoginEvent
    data class RememberMeChanged(val checked: Boolean): LoginEvent
    data class FacebookLoginClicked(val token: String): LoginEvent
    data class GoogleLoginClicked(val token: String): LoginEvent
    data class ErrorMessageSet(val message: String): LoginEvent
    object LoginClicked: LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateInputUseCase: ValidateAuthInputUseCase,
    private val loginWithFacebookUseCase: LoginWithFacebookUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state =  _state.asStateFlow()

    fun onEvent (event: LoginEvent) {
        when (event) {
            is LoginEvent.PhoneChanged -> {
                if (event.phone.all { it.isDigit() }) {
                    _state.update { it.copy(phone = event.phone, phoneError = null, errorMessage = null) }
                }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, passwordError = null, errorMessage = null) }
            }
            is LoginEvent.RememberMeChanged -> {
                _state.update { it.copy(rememberMe = event.checked) }
            }
            is LoginEvent.FacebookLoginClicked -> loginWithFacebook(event.token)
            is LoginEvent.GoogleLoginClicked -> loginWithGoogle(event.token)
            is LoginEvent.ErrorMessageSet -> _state.update { it.copy(errorMessage = event.message) }
            LoginEvent.LoginClicked -> login()
        }
    }
    private fun validateInput() : Boolean {
        val currentState = _state.value
        val phoneError = validateInputUseCase.validatePhone(currentState.phone)
        val passwordError = validateInputUseCase.validatePassword(currentState.password)

        val hasError = listOf(phoneError, passwordError).any { it != null}
        if (hasError) {
            _state.update {
                it.copy(
                    phoneError = phoneError,
                    passwordError = passwordError
                )
            }
        }
        return !hasError
    }

    private fun loginWithFacebook(facebookToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = loginWithFacebookUseCase(facebookToken)
            result.onSuccess { roles ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        postLoginDestination = resolveStartDestination(roles)
                    )
                }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login with Facebook failed"
                    )
                }
            }
        }
    }

    private fun loginWithGoogle(googleToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = loginWithGoogleUseCase(googleToken)
            result.onSuccess { roles ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        postLoginDestination = resolveStartDestination(roles)
                    )
                }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login with Google failed"
                    )
                }
            }
        }
    }

    private fun login() {
        if (!validateInput()) return
        val currentState = _state.value

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            val result = loginUseCase(
                phone = currentState.phone,
                password = currentState.password,
                rememberMe = currentState.rememberMe
            )
            
            result.onSuccess { roles ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        postLoginDestination = resolveStartDestination(roles)
                    )
                }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            }
        }
    }
}