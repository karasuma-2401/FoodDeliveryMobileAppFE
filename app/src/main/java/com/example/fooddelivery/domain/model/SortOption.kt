package com.example.fooddelivery.domain.model

enum class SearchSortOption(val apiValue: String, val title: String) {
    DISTANCE("distance", "Nearest"),
    RATING("rating", "Top Rated"),
    PRICE_LOW_TO_HIGH("price_low_to_high", "Lowest Price")
}

enum class RestaurantSortOption(val apiValue: String?, val title: String) {
    NEWEST(null, "Newest"),
    DISTANCE("DISTANCE", "Near me"),
    RATING("RATING", "Top rated"),
}

enum class RestaurantMinRatingFilter(val value: Double?, val title: String) {
    ANY(null, "All ratings"),
    THREE(3.0, "3+ stars"),
    FOUR(4.0, "4+ stars"),
    FOUR_FIVE(4.5, "4.5+ stars"),
}

enum class FoodSortOption(val title: String) {
    POPULARITY("Popular"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    RATING("Rating")
}
