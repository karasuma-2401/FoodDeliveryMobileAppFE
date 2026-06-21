package com.example.fooddelivery.ui.screens.profile.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Notification
import com.example.fooddelivery.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val isEndReached: Boolean = false,
    val page: Int = 1,
    val errorMessage: String? = null
)

sealed interface NotificationEvent {
    object LoadNotifications: NotificationEvent
    object LoadMore: NotificationEvent
    data class MarkAsRead(val id: String): NotificationEvent
    object MarkAllRead : NotificationEvent
    object ErrorDismissed : NotificationEvent
    object RefreshUnreadCount : NotificationEvent
}
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    private val pageSize = 10
    init {
        onEvent(NotificationEvent.LoadNotifications)
        onEvent(NotificationEvent.RefreshUnreadCount)
    }
    fun onEvent(event: NotificationEvent) {
        when(event) {
            NotificationEvent.LoadNotifications -> loadInitialNotification()
            NotificationEvent.LoadMore -> loadMoreNotifications()
            is NotificationEvent.MarkAsRead -> markAsRead(event.id)
            NotificationEvent.MarkAllRead -> markAllRead()
            NotificationEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
            NotificationEvent.RefreshUnreadCount -> loadUnreadCount()
        }
    }
    private fun loadInitialNotification() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, page = 1, isEndReached = false) }
            val result = repository.getNotificationsPaged(1, pageSize)
            result.onSuccess { list ->
                _state.update { it.copy(
                    notifications = list,
                    isLoading = false,
                    isEndReached = list.size < pageSize
                ) }
            }.onFailure { exception ->
                _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }
    private fun loadMoreNotifications() {
        val currentState = _state.value
        if (currentState.isPaginating || currentState.isEndReached) return

        viewModelScope.launch {
            _state.update { it.copy(isPaginating = true) }
            val nextPage = currentState.page + 1
            val result = repository.getNotificationsPaged(nextPage, pageSize)
            result.onSuccess { list ->
                _state.update { it.copy(
                    notifications = currentState.notifications + list,
                    isPaginating = false,
                    page = nextPage,
                    isEndReached = list.size < pageSize
                ) }
            }
        }
    }
    private fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
            _state.update { state ->
                state.copy(
                    notifications = state.notifications.map {
                        if (it.id == id) it.copy(isRead = true) else it
                    },
                    unreadCount = (state.unreadCount - 1).coerceAtLeast(0)
                )
            }
        }
    }
    private fun markAllRead() {
        viewModelScope.launch {
            repository.markAllRead()
            _state.update { state ->
                state.copy(
                    notifications = state.notifications.map { it.copy(isRead = true)},
                    unreadCount = 0
                )
            }
        }
    }
    private fun loadUnreadCount() {
        viewModelScope.launch {
            repository.getUnreadCount().onSuccess { count ->
                _state.update { it.copy(unreadCount = count) }
            }
        }
    }
}