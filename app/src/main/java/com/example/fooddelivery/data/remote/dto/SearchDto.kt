package com.example.fooddelivery.data.remote.dto

import com.example.fooddelivery.data.remote.MediaUrlResolver
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.SearchHistory
import com.example.fooddelivery.domain.model.TrendingKeyword
import com.example.fooddelivery.domain.util.pickBestBadgeLabel
import kotlinx.serialization.Serializable

@Serializable
data class UnifiedSearchResponse(
    val foods: List<FoodSearchDto>,
    val restaurants: List<RestaurantSearchDto>,
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
    val hasVoucher: Boolean,
    val vouchers: List<VoucherDto> = emptyList(),
)

@Serializable
data class SearchHistoryDto(
    val id: Int,
    val keyword: String,
    val userId: Int? = null,
    val createdAt: String? = null,
)

@Serializable
data class TrendingKeywordDto(
    val keyword: String,
    val searchCount: Int,
)

fun UnifiedSearchResponse.toDomain(): Pair<List<FoodItem>, List<Restaurant>> {
    val voucherByRestaurantId = restaurants.associate { dto ->
        dto.id.toString() to dto.vouchers.pickBestBadgeLabel()
    }
    val restaurantDomains = restaurants.map { it.toDomain() }
    val foodDomains = foods.map { dto ->
        dto.toDomain(voucherLabel = voucherByRestaurantId[dto.restaurantId.toString()])
    }
    return foodDomains to restaurantDomains
}

fun FoodSearchDto.toDomain(voucherLabel: String? = null) = FoodItem(
    id = id.toString(),
    name = name,
    price = price,
    imageUrl = imageUrl?.takeIf { it.isNotBlank() }?.let { MediaUrlResolver.resolve(it) },
    restaurantId = restaurantId.toString(),
    restaurantName = restaurantName,
    rating = rating,
    soldCount = soldCount,
    voucherBadgeLabel = voucherLabel,
)

fun RestaurantSearchDto.toDomain() = Restaurant(
    id = id.toString(),
    name = name,
    rating = averageRating,
    deliveryFee = deliveryFee,
    imageUrl = imageUrl?.takeIf { it.isNotBlank() }?.let { MediaUrlResolver.resolve(it) },
    distance = distance,
    tags = tags,
    hasVoucher = hasVoucher,
    voucherBadgeLabel = vouchers.pickBestBadgeLabel(),
)

fun SearchHistoryDto.toDomain() = SearchHistory(
    id = id,
    keyword = keyword,
)

fun TrendingKeywordDto.toDomain() = TrendingKeyword(
    keyword = keyword,
    searchCount = searchCount,
)
