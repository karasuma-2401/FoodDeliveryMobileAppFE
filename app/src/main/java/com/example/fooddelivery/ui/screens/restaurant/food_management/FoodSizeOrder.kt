package com.example.fooddelivery.ui.screens.restaurant.food_management

val FOOD_SIZE_ORDER = listOf("S", "M", "L", "XL")

fun foodSizeSortKey(size: String): Int =
    FOOD_SIZE_ORDER.indexOf(size.uppercase()).takeIf { it >= 0 } ?: Int.MAX_VALUE

fun mapFoodSizeToId(size: String): Int = when (size.uppercase()) {
    "S" -> 1
    "M" -> 2
    "L" -> 3
    "XL" -> 4
    else -> 2
}

fun Iterable<String>.sortedByFoodSize(): List<String> = sortedBy { foodSizeSortKey(it) }
