package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.HomeDashboardDto
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.model.Address

data class HomeDashboardData(
    val user: User?,
    val categories: List<Category>,
    val restaurants: List<Restaurant>,
    val addresses: List<Address>,
    val cartItemCount: Int,
    val unreadMessageCount: Int
)

interface HomeRepository {
    suspend fun getHomeDashboard(lat: Double?, lng: Double?): Result<HomeDashboardData>
}
