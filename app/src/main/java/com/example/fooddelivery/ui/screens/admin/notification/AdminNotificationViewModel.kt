package com.example.fooddelivery.ui.screens.admin.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminNotificationViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(AdminNotificationState())
    val state: StateFlow<AdminNotificationState> = _state.asStateFlow()

    init {
        loadNotifications()
    }

    fun onEvent(event: AdminNotificationEvent) {
        when (event) {
            is AdminNotificationEvent.ApproveVendor -> approveVendor(event.vendorId)
            is AdminNotificationEvent.RejectVendor -> rejectVendor(event.vendorId)
            AdminNotificationEvent.Refresh -> loadNotifications()
            AdminNotificationEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val mockData = listOf(
                VendorNotification("1", "Bún Chả Obama - Cơ sở Q1", "Nguyễn Văn A", "obamaq1@gmail.com", "10 mins ago"),
                VendorNotification("2", "Pizza Hut - Thủ Đức", "Trần Thị B", "pzhthuduc@gmail.com", "2 hours ago"),
                VendorNotification("3", "Trà Sữa Phê La - Bình Thạnh", "Lê Văn C", "phela.bt@gmail.com", "Yesterday")
            )
            _state.update { it.copy(notifications = mockData, isLoading = false) }
        }
    }

    private fun approveVendor(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            _state.update { currentState ->
                currentState.copy(
                    notifications = currentState.notifications.filter { it.id != id },
                    isLoading = false
                )
            }
        }
    }

    private fun rejectVendor(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(600) // Gọi API từ chối vendor ngầm
            _state.update { currentState ->
                currentState.copy(
                    notifications = currentState.notifications.filter { it.id != id },
                    isLoading = false
                )
            }
        }
    }
}