package com.example.fooddelivery.ui.screens.home.location

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LocationState(
    val isPermissionGranted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LocationEvent {
    data class PermissionResult(val isGranted: Boolean) : LocationEvent
    object RequestPermissionClicked : LocationEvent
    object ErrorDismissed : LocationEvent
}

@HiltViewModel
class LocationViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(LocationState())
    val state: StateFlow<LocationState> = _state.asStateFlow()

    fun onEvent(event: LocationEvent) {
        when (event) {
            is LocationEvent.PermissionResult -> {
                _state.update { it.copy(isPermissionGranted = event.isGranted, isLoading = false) }
            }
            LocationEvent.RequestPermissionClicked -> {
                _state.update { it.copy(isLoading = true) }
            }
            LocationEvent.ErrorDismissed -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }
}
