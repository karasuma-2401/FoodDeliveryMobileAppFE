package com.example.fooddelivery.domain.model

data class RestaurantListQuery(
    val limit: Int = 10,
    val offset: Int = 0,
    val keyword: String? = null,
    val categoryId: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val minRating: Double? = null,
    val sort: RestaurantSortOption = RestaurantSortOption.NEWEST,
)
