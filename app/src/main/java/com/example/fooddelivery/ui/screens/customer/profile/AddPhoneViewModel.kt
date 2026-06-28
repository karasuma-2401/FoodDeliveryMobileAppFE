package com.example.fooddelivery.ui.screens.customer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.usecase.AddUserPhoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPhoneState(
    val phone: String = "",
    val phoneError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddPhoneEvent {
    data class PhoneChanged(val phone: String) : AddPhoneEvent
    object SaveClicked : AddPhoneEvent
    object ErrorDismissed : AddPhoneEvent
    object ResetSuccessState : AddPhoneEvent
}

@HiltViewModel
class AddPhoneViewModel @Inject constructor(
    private val addUserPhoneUseCase: AddUserPhoneUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AddPhoneState())
    val state: StateFlow<AddPhoneState> = _state.asStateFlow()

    fun onEvent(event: AddPhoneEvent) {
        when (event) {
            is AddPhoneEvent.PhoneChanged -> {
                if (event.phone.all { it.isDigit() }) {
                    _state.update { it.copy(phone = event.phone, phoneError = null, errorMessage = null) }
                }
            }
            AddPhoneEvent.SaveClicked -> savePhone()
            AddPhoneEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
            AddPhoneEvent.ResetSuccessState -> _state.update { it.copy(isSuccess = false) }
        }
    }

    private fun savePhone() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, phoneError = null, errorMessage = null) }
            addUserPhoneUseCase(_state.value.phone)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to add phone number"
                        )
                    }
                }
        }
    }
}
