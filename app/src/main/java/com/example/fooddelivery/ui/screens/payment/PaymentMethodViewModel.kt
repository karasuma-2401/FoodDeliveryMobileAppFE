package com.example.fooddelivery.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.LinkedPaymentMethod
import com.example.fooddelivery.domain.model.PaymentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PaymentMethodState(
    val linkedMoMoMethods: List<LinkedPaymentMethod> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface PaymentMethodEvent {
    data object LoadMethods : PaymentMethodEvent
    data class UnlinkMoMo(val id: String) : PaymentMethodEvent
    data object LinkNewMoMo : PaymentMethodEvent
    data object ErrorDismissed : PaymentMethodEvent
}

@HiltViewModel
class PaymentMethodViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(PaymentMethodState())
    val state = _state.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(800) 
            val mockData = listOf(
                LinkedPaymentMethod(
                    id = "1",
                    type = PaymentType.MOMO,
                    name = "Ví MoMo",
                    identifier = "09****99",
                    token = "momo_token_mock_1",
                    isDefault = true
                )
            )
            _state.update { it.copy(linkedMoMoMethods = mockData, isLoading = false) }
        }
    }

    fun onEvent(event: PaymentMethodEvent) {
        when (event) {
            is PaymentMethodEvent.LoadMethods -> loadMockData()
            is PaymentMethodEvent.UnlinkMoMo -> {
                _state.update { currentState ->
                    currentState.copy(
                        linkedMoMoMethods = currentState.linkedMoMoMethods.filter { it.id != event.id }
                    )
                }
            }
            is PaymentMethodEvent.LinkNewMoMo -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    delay(2000)
                    val newMethod = LinkedPaymentMethod(
                        id = UUID.randomUUID().toString(),
                        type = PaymentType.MOMO,
                        name = "Ví MoMo",
                        identifier = "03****88",
                        token = "momo_token_${System.currentTimeMillis()}"
                    )
                    _state.update { currentState ->
                        currentState.copy(
                            linkedMoMoMethods = currentState.linkedMoMoMethods + newMethod,
                            isLoading = false
                        )
                    }
                }
            }
            is PaymentMethodEvent.ErrorDismissed -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }
}
