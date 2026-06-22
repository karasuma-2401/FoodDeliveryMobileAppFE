package com.example.fooddelivery.ui.screens.auth.role

enum class UserRole { CUSTOMER, RESTAURANT }

data class RoleSelectionState(
    val selectedRole: UserRole? = null
)