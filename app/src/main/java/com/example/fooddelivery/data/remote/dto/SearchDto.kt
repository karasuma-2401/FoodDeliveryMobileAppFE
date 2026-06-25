package com.example.fooddelivery.data.remote.dto

import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.SearchHistory
import kotlinx.serialization.Serializable

@Serializable
data class UnifiedSearchResponse(
    val foods: List<FoodSearchDto>,
    val restaurants: List<RestaurantSearchDto>
)

@Serializable
data class FoodSearchDto(
    val id: Int,
    val name: String,
    val price: Double,
    val imageUrl: String?,
    val restaurantId: Int,
    val restaurantName: String,
    val rating: Float,
    val soldCount: Int,
    val promoTag: String?
)

@Serializable
data class RestaurantSearchDto(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val averageRating: Float,
    val deliveryFee: Double,
    val distance: Double?,
    val tags: List<String>,
    val hasVoucher: Boolean
)

@Serializable
data class SearchHistoryDto(
    val id: Int,
    val keyword: String,
    val userId: Int? = null,
    val createdAt: String? = null
)

@Serializable
data class SearchNestedPayload<T>(
    val success: Boolean? = null,
    val message: String? = null,
    val data: T? = null
)

@Serializable
data class TrendingKeywordDto(
    val keyword: String,
    val searchCount: Int
)

fun FoodSearchDto.toDomain() = FoodItem(
    id = id.toString(),
    name = name,
    price = price,
    imageUrl = imageUrl,
    restaurantId = restaurantId.toString(),
    restaurantName = restaurantName,
    rating = rating,
    soldCount = soldCount,
    promoTag = promoTag
)

fun RestaurantSearchDto.toDomain() = Restaurant(
    id = id.toString(),
    name = name,
    rating = averageRating,
    deliveryFee = deliveryFee,
    imageUrl = imageUrl,
    distance = distance,
    tags = tags,
    hasVoucher = hasVoucher
)

fun SearchHistoryDto.toDomain() = SearchHistory(
    id = id,
    keyword = keyword
)
