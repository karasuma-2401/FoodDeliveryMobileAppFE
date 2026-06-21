package com.example.fooddelivery.ui.screens.admin.notification

import com.example.fooddelivery.domain.model.Notification

data class AdminNotificationState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val isEndReached: Boolean = false,
    val page: Int = 1,
    val errorMessage: String? = null
)

sealed interface AdminNotificationEvent {
    object LoadNotifications: AdminNotificationEvent
    object LoadMore: AdminNotificationEvent

    data class ApproveVendor(val notificationId: String, val vendorId: String) : AdminNotificationEvent
    data class RejectVendor(val notificationId: String, val vendorId: String) : AdminNotificationEvent
    object ErrorDismissed : AdminNotificationEvent
}