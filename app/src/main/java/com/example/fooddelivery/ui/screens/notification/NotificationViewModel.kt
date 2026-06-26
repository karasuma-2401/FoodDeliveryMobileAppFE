package com.example.fooddelivery.ui.screens.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Notification
import com.example.fooddelivery.domain.repository.AdminRepository
import com.example.fooddelivery.domain.repository.NotificationRepository
import com.example.fooddelivery.domain.repository.OrderRepository
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
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val processingNotificationId: String? = null
)

sealed interface NotificationEvent {
    object LoadNotifications : NotificationEvent
    object LoadMore : NotificationEvent
    data class MarkAsRead(val id: String) : NotificationEvent
    object MarkAllRead : NotificationEvent
    data class DeleteNotification(val id: String) : NotificationEvent
    data class ExecuteAction(val notificationId: String, val action: String) : NotificationEvent
    object ErrorDismissed : NotificationEvent
    object SuccessDismissed : NotificationEvent
    object RefreshUnreadCount : NotificationEvent
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val orderRepository: OrderRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    private val pageSize = 10

    init {
        onEvent(NotificationEvent.LoadNotifications)
        onEvent(NotificationEvent.RefreshUnreadCount)
    }

    fun onEvent(event: NotificationEvent) {
        when (event) {
            NotificationEvent.LoadNotifications -> loadInitialNotification()
            NotificationEvent.LoadMore -> loadMoreNotifications()
            is NotificationEvent.MarkAsRead -> markAsRead(event.id)
            NotificationEvent.MarkAllRead -> markAllRead()
            is NotificationEvent.DeleteNotification -> deleteNotification(event.id)
            is NotificationEvent.ExecuteAction -> executeAction(event.notificationId, event.action)
            NotificationEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
            NotificationEvent.SuccessDismissed -> _state.update { it.copy(successMessage = null) }
            NotificationEvent.RefreshUnreadCount -> loadUnreadCount()
        }
    }

    private fun loadInitialNotification() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, page = 1, isEndReached = false) }
            val result = repository.getNotificationsPaged(1, pageSize)
            result.onSuccess { list ->
                _state.update {
                    it.copy(
                        notifications = list,
                        isLoading = false,
                        isEndReached = list.size < pageSize
                    )
                }
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
                _state.update {
                    it.copy(
                        notifications = currentState.notifications + list,
                        isPaginating = false,
                        page = nextPage,
                        isEndReached = list.size < pageSize
                    )
                }
            }.onFailure {
                _state.update { it.copy(isPaginating = false) }
            }
        }
    }

    private fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
            _state.update { state ->
                val wasUnread = state.notifications.find { it.id == id }?.isRead == false
                state.copy(
                    notifications = state.notifications.map {
                        if (it.id == id) it.copy(isRead = true) else it
                    },
                    unreadCount = if (wasUnread) (state.unreadCount - 1).coerceAtLeast(0) else state.unreadCount
                )
            }
        }
    }

    private fun markAllRead() {
        viewModelScope.launch {
            repository.markAllRead()
            _state.update { state ->
                state.copy(
                    notifications = state.notifications.map { it.copy(isRead = true) },
                    unreadCount = 0
                )
            }
        }
    }

    private fun deleteNotification(id: String) {
        viewModelScope.launch {
            val wasUnread = _state.value.notifications.find { it.id == id }?.isRead == false
            repository.deleteNotification(id)
            _state.update { state ->
                state.copy(
                    notifications = state.notifications.filter { it.id != id },
                    unreadCount = if (wasUnread) (state.unreadCount - 1).coerceAtLeast(0) else state.unreadCount
                )
            }
        }
    }

    private fun executeAction(notificationId: String, action: String) {
        val notification = _state.value.notifications.find { it.id == notificationId } ?: return
        val targetId = notification.targetId?.toIntOrNull()
            ?: run {
                _state.update { it.copy(errorMessage = "Invalid notification target") }
                return
            }

        viewModelScope.launch {
            _state.update { it.copy(processingNotificationId = notificationId, errorMessage = null) }

            val result = when (action.uppercase()) {
                "ACCEPT_ORDER" -> orderRepository.updateOrderStatus(targetId, "PREPARING")
                "REJECT_ORDER" -> orderRepository.updateOrderStatus(targetId, "CANCELLED")
                "CONFIRM_RECEIVED" -> orderRepository.confirmReceived(targetId)
                "APPROVE_VENDOR", "APPROVE" -> adminRepository.updateRestaurantApproval(targetId, "APPROVED")
                "REJECT_VENDOR", "REJECT" -> adminRepository.updateRestaurantApproval(targetId, "REJECTED")
                else -> Result.failure(IllegalArgumentException("Unsupported action: $action"))
            }

            result.fold(
                onSuccess = {
                    repository.markAsRead(notificationId)
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.filter { it.id != notificationId },
                            processingNotificationId = null,
                            unreadCount = if (!notification.isRead) {
                                (state.unreadCount - 1).coerceAtLeast(0)
                            } else {
                                state.unreadCount
                            },
                            successMessage = actionSuccessMessage(action)
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            processingNotificationId = null,
                            errorMessage = error.message ?: "Action failed"
                        )
                    }
                }
            )
        }
    }

    private fun actionSuccessMessage(action: String): String {
        return when (action.uppercase()) {
            "ACCEPT_ORDER" -> "Order accepted"
            "REJECT_ORDER" -> "Order rejected"
            "CONFIRM_RECEIVED" -> "Order confirmed as received"
            "APPROVE_VENDOR", "APPROVE" -> "Restaurant approved"
            "REJECT_VENDOR", "REJECT" -> "Restaurant rejected"
            else -> "Action completed"
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
