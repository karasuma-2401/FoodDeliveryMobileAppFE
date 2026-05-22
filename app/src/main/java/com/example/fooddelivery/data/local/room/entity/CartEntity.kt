package com.example.fooddelivery.data.local.room.entity

import androidx.room.Entity
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.model.FoodItem

@Entity(
    tableName = "cart_items",
    primaryKeys = ["foodId", "size", "restaurantId"]
)
data class CartEntity(
    val foodId: String,
    val size: String,
    val restaurantId: String,
    val quantity: Int,
    val unitPrice: Double,
    val restaurantName: String,
    val foodName: String,
    val foodPrice: Double,
    val foodImageUrl: String?,
    val foodImageRes: Int?
)

fun CartEntity.toDomain(): CartItem {
    return CartItem(
        food = FoodItem(
            id = foodId,
            name = foodName,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            categoryId = "",
            price = foodPrice,
            rating = 0f,
            reviewCount = 0,
            soldCount = 0,
            imageUrl = foodImageUrl,
            imageRes = foodImageRes,
            promoTag = null
        ),
        size = size,
        quantity = quantity,
        unitPrice = unitPrice,
        restaurantId = restaurantId,
        restaurantName = restaurantName
    )
}

fun CartItem.toEntity(): CartEntity {
    return CartEntity(
        foodId = food.id,
        size = size,
        restaurantId = restaurantId,
        quantity = quantity,
        unitPrice = unitPrice,
        restaurantName = restaurantName,
        foodName = food.name,
        foodPrice = food.price,
        foodImageUrl = food.imageUrl,
        foodImageRes = food.imageRes
    )
}
