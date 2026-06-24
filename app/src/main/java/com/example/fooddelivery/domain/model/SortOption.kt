package com.example.fooddelivery.domain.model

enum class SearchSortOption(val apiValue: String, val title: String) {
    DISTANCE("distance", "Nearest"),
    RATING("rating", "Top Rated"),
    PRICE_LOW_TO_HIGH("price_low_to_high", "Lowest Price")
}

enum class RestaurantSortOption(val title: String) {
    RATING("Best Rating"),
    DELIVERY_FEE("Delivery fee"),
}

enum class FoodSortOption(val title: String) {
    POPULARITY("Popular"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    RATING("Rating")
}
