package com.example.fooddelivery.ui.screens.admin.notification
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Notification
import com.example.fooddelivery.domain.model.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminNotificationViewModel @Inject constructor(
) : ViewModel() {

    private val _state = MutableStateFlow(AdminNotificationState())
    val state: StateFlow<AdminNotificationState> = _state.asStateFlow()

    private val pageSize = 5

    private var mockDatabase = mutableListOf<Notification>()

    init {
        setupMockDatabase()
        loadInitialNotification()
    }

    fun onEvent(event: AdminNotificationEvent) {
        when (event) {
            AdminNotificationEvent.LoadNotifications -> loadInitialNotification()
            AdminNotificationEvent.LoadMore -> loadMoreNotifications()
            is AdminNotificationEvent.ApproveVendor -> approveVendor(event.notificationId)
            is AdminNotificationEvent.RejectVendor -> rejectVendor(event.notificationId)
            AdminNotificationEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun setupMockDatabase() {
        mockDatabase.clear()
        for (i in 1..15) {
            mockDatabase.add(
                Notification(
                    id = "notif_$i",
                    title = "New Vendor Registration",
                    message = "Vendor Request #$i from owner@mail.com",
                    isRead = false,
                    targetId = "vendor_id_$i",
                    timestamp = System.currentTimeMillis(),
                    type = NotificationType.SYSTEM
                )
            )
        }
    }

    private fun loadInitialNotification() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, page = 1, isEndReached = false) }

            val initialData = mockDatabase.take(pageSize)

            _state.update {
                it.copy(
                    notifications = initialData,
                    isLoading = false,
                    isEndReached = initialData.size == mockDatabase.size
                )
            }
        }
    }

    private fun loadMoreNotifications() {
        val currentState = _state.value
        if (currentState.isPaginating || currentState.isEndReached) return

        viewModelScope.launch {
            _state.update { it.copy(isPaginating = true) }

            val nextPage = currentState.page + 1
            val startIndex = currentState.page * pageSize
            val endIndex = minOf(startIndex + pageSize, mockDatabase.size)

            val nextData = if (startIndex < mockDatabase.size) {
                mockDatabase.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

            _state.update {
                it.copy(
                    notifications = currentState.notifications + nextData,
                    isPaginating = false,
                    page = nextPage,
                    isEndReached = endIndex >= mockDatabase.size
                )
            }
        }
    }

    private fun approveVendor(notificationId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            mockDatabase.removeAll { it.id == notificationId }

            _state.update { currentState ->
                currentState.copy(
                    notifications = currentState.notifications.filter { it.id != notificationId },
                    isLoading = false
                )
            }
        }
    }

    private fun rejectVendor(notificationId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            mockDatabase.removeAll { it.id == notificationId }

            _state.update { currentState ->
                currentState.copy(
                    notifications = currentState.notifications.filter { it.id != notificationId },
                    isLoading = false
                )
            }
        }
    }
}