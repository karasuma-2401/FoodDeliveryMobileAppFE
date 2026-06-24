package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.HomeApi
import com.example.fooddelivery.data.remote.dto.HomeDashboardDto
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.repository.HomeDashboardData
import com.example.fooddelivery.domain.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeApi: HomeApi
) : HomeRepository {
    override suspend fun getHomeDashboard(lat: Double?, lng: Double?): Result<HomeDashboardData> {
        return try {
            val response = homeApi.getDashboard(lat, lng)
            if (response.success != false && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun HomeDashboardDto.toDomain(): HomeDashboardData {
        return HomeDashboardData(
            user = user?.let {
                User(
                    id = it.id.toString(),
                    fullName = it.fullName,
                    profileImage = it.avatarUrl,
                    phone = it.phone ?: ""
                )
            },
            categories = categories.map {
                Category(
                    id = it.id.toString(),
                    name = it.name,
                    imageUrl = it.imageUrl
                )
            },
            restaurants = restaurants.map {
                Restaurant(
                    id = it.id.toString(),
                    name = it.name,
                    imageUrl = it.imageUrl,
                    rating = it.averageRating.toFloat(),
                    reviewCount = it.reviewCount,
                    deliveryFee = it.deliveryFee,
                    distance = it.distance,
                    tags = it.tags,
                    estimatedDeliveryTime = it.estimatedDeliveryTime,
                    isLiked = it.isLiked
                )
            },
            addresses = addresses.map {
                Address(
                    id = it.id,
                    type = it.title,
                    detail = it.fullText
                )
            },
            cartItemCount = counters.cartItemCount,
            unreadMessageCount = counters.unreadMessageCount
        )
    }
}
