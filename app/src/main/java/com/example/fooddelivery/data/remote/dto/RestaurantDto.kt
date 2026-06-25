package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
@Serializable
data class RestaurantDashboardRangeResponse(
    val deliveredRevenue: Double,
    val deliveredOrderCount: Int,
    val cancelledOrderCount: Int,
    val topFoods: List<RestaurantDashboardTopFoodDto> = emptyList()
)

@Serializable
data class RestaurantDashboardTopFoodDto(
    val id: Int,
    val name: String,
    val image: String? = null,
    val quantity: Int,
    val revenue: Double
)

@Serializable
data class DashboardRecentOrderDto(
    val id: String,
    val orderNumber: String,
    val customerName: String,
    val totalPrice: Double,
    val status: String,
    val time: String
)

@Serializable
data class DashboardBestSellerDto(
    val id: Int,
    val name: String,
    val price: Double,
    val rating: Double,
    val soldCount: Int,
    val imageUrl: String? = null
)

@Serializable
data class RestaurantDashboardResponse(
    val runningOrders: Int,
    val orderRequest: Int,
    val revenue: Double,
    val rating: Double,
    val totalReviews: Int,
    val totalOrders: Int,
    val activeVouchers: Int,
    val recentOrders: List<DashboardRecentOrderDto> = emptyList(),
    val bestSellers: List<DashboardBestSellerDto> = emptyList()
)

@Serializable
data class FoodRequest(
    val name: String,
    val price: Double,
    val details: String,
    val category: String,
    val imageUrl: String? = null
)

@Serializable
data class FoodSizeRequest(
    val sizeId: Int,
    val price: Double,
    @SerialName("isDefault")
    val isDefault: Boolean = false
)

@Serializable
data class FoodSizeOptionDto(
    val foodSizeId: Int,
    val sizeId: Int,
    val name: String,
    val price: Double,
    val isDefault: Boolean = false
)

@Serializable
data class IngredientDto(
    val id: Int,
    val name: String,
    val icon: String? = null
)

@Serializable
data class FoodRestaurantDto(
    val id: Int,
    val name: String,
    val image: String? = null,
    val coverImage: String? = null
)

@Serializable
data class FoodCategoryDto(
    val id: Int,
    val name: String
)

@Serializable
data class FoodResponse(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val categoryId: Int,
    val restaurantId: Int,
    val label: String? = null,
    val isAvailable: Boolean = true,
    val image: String? = null,
    val rating: Float? = null,
    val reviewCount: Int? = null,
    val category: FoodCategoryDto? = null,
    val restaurant: FoodRestaurantDto? = null,
    val foodIngredients: List<IngredientDto>? = null,
    val sizes: List<FoodSizeOptionDto>? = null
)

@Serializable
data class BaseResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val success: Boolean? = null
)

@Serializable
data class RestaurantResponse(
    val id: Int,
    val name: String,
    val image: String? = null,
    val coverImage: String? = null,
    val description: String? = null,
    val phone: String? = null,
    val deliveryFee: Double? = null,
    val minimumOrder: Double? = null,
    val estimatedDeliveryTime: Int? = null,
    val address: RestaurantAddressDto? = null,
    val averageRating: Double? = null,
    val rating: Double? = null, // Dùng cho API /user/favorites
    val ratingCount: Int? = null,
    val categories: List<RestaurantCategoryDto>? = null,
    val tags: List<String>? = null, // Dùng cho API /user/favorites
    val startingPrice: Double? = null,
    val isLiked: Boolean? = false,
    val totalLikes: Int? = 0
)

@Serializable
data class LikeStatusResponse(
    val restaurantId: Int? = null,
    val isLiked: Boolean,
    val totalLikes: Int? = null
)

@Serializable
data class ToggleFavoritePayload(
    val success: Boolean? = null,
    val message: String? = null,
    val data: LikeStatusResponse? = null,
    // Backward-compatible with flat payload shape if backend changes again
    val restaurantId: Int? = null,
    val isLiked: Boolean? = null,
    val totalLikes: Int? = null
)

fun ToggleFavoritePayload.toLikeStatusResponse(): LikeStatusResponse? {
    return data ?: isLiked?.let {
        LikeStatusResponse(
            restaurantId = restaurantId,
            isLiked = it,
            totalLikes = totalLikes
        )
    }
}

@Serializable
data class RestaurantAddressDto(
    val id: Int,
    val title: String,
    val fullText: String
)

@Serializable
data class RestaurantCategoryDto(
    val id: Int,
    val name: String
)

@Serializable
data class RestaurantMenuCategoryResponse(
    val id: Int,
    val name: String,
    val image: String? = "",
    val description: String? = "",
    val sortOrder: Int? = 0,
    val isActive: Boolean? = true,
    val displayOrder: Int? = 0,
    val foodCount: Int? = 0,
    val foods: List<NestedFoodDto> = emptyList()
)

@Serializable
data class NestedFoodDto(
    val id: Int,
    val name: String? = null,
    val price: Double? = null,
    val description: String? = null,
    val image: String? = null
)
