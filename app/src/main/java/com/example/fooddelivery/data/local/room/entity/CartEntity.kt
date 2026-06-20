package com.example.fooddelivery.data.local.room.entity

import androidx.room.Entity
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.model.FoodItem

@Entity(
    tableName = "cart_items",
    primaryKeys = ["foodId", "restaurantId", "foodSize"]
)
data class CartEntity(
    val foodId: String,
    val restaurantId: String,
    val quantity: Int,
    val unitPrice: Double,
    val restaurantName: String,
    val foodName: String,
    val foodPrice: Double,
    val foodImageUrl: String?,
    val foodImageRes: Int?,
    val categoryId: String,
    val rating: Float,
    val reviewCount: Int,
    val soldCount: Int,
    val promoTag: String?,
    val foodSize: String,
    val cartItemId: Int? = null 
)

fun CartEntity.toDomain(): CartItem {
    return CartItem(
        food = FoodItem(
            id = foodId,
            name = foodName,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            categoryId = categoryId,
            price = foodPrice,
            rating = rating,
            reviewCount = reviewCount,
            soldCount = soldCount,
            imageUrl = foodImageUrl,
            imageRes = foodImageRes,
            promoTag = promoTag,
            size = if (foodSize.isEmpty()) null else foodSize
        ),
        quantity = quantity,
        unitPrice = unitPrice,
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        cartItemId = cartItemId
    )
}

fun CartItem.toEntity(): CartEntity {
    return CartEntity(
        foodId = food.id,
        restaurantId = restaurantId,
        quantity = quantity,
        unitPrice = unitPrice,
        restaurantName = restaurantName,
        foodName = food.name,
        foodPrice = food.price,
        foodImageUrl = food.imageUrl,
        foodImageRes = food.imageRes,
        categoryId = food.categoryId,
        rating = food.rating,
        reviewCount = food.reviewCount,
        soldCount = food.soldCount,
        promoTag = food.promoTag,
        foodSize = food.size ?: "",
        cartItemId = cartItemId
    )
}
