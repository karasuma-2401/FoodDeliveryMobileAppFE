package com.example.fooddelivery.ui.screens.admin.notification

data class VendorNotification(
    val id: String,
    val restaurantName: String,
    val ownerName: String,
    val email: String,
    val timestamp: String,
    val isPending: Boolean = true
)

data class AdminNotificationState(
    val notifications: List<VendorNotification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AdminNotificationEvent {
    data class ApproveVendor(val vendorId: String) : AdminNotificationEvent
    data class RejectVendor(val vendorId: String) : AdminNotificationEvent
    object Refresh : AdminNotificationEvent
    object ErrorDismissed : AdminNotificationEvent
}