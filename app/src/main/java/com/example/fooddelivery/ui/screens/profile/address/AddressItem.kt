package com.example.fooddelivery.ui.screens.profile.address

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class AddressItem(
    val id: String,
    val type: String,
    val title: String,
    val detail: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBgColor: Color
)
