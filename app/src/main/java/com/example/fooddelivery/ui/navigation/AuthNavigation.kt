package com.example.fooddelivery.ui.navigation

import com.example.fooddelivery.data.remote.dto.MeResponse

fun resolveStartDestination(roles: List<String>): Any = when {
    roles.contains("ADMIN") -> AdminGraph
    roles.contains("BUSINESS") -> RestaurantGraph
    else -> CustomerGraph
}

fun MeResponse.toStartDestination(): Any = resolveStartDestination(roles)
