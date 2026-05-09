package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.RestaurantApi
import com.example.fooddelivery.data.remote.dto.*
import com.example.fooddelivery.domain.repository.RestaurantRepository
import javax.inject.Inject

class RestaurantRepositoryImpl @Inject constructor(
    private val api: RestaurantApi
) : RestaurantRepository {

    override suspend fun getDashboard(): Result<DashboardResponse> {
        // Mock Data for UI Testing
        return Result.success(
            DashboardResponse(
                runningOrders = 12,
                orderRequest = 8,
                revenue = 2500.0,
                rating = 4.8,
                totalReviews = 150
            )
        )
        /*
        return try {
            val response = api.getDashboard()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        */
    }

    override suspend fun getFoods(): Result<List<FoodResponse>> {
        // Mock Data for UI Testing
        return Result.success(
            listOf(
                FoodResponse(
                    id = "1",
                    name = "Chicken Burger",
                    price = 15.0,
                    details = "Delicious chicken burger with cheese",
                    category = "Lunch",
                    rating = 4.5f,
                    reviewCount = 20,
                    imageUrl = "https://images.unsplash.com/photo-1571091718767-18b5b1457add"
                ),
                FoodResponse(
                    id = "2",
                    name = "Pizza Margherita",
                    price = 20.0,
                    details = "Classic pizza with tomato and mozzarella",
                    category = "Dinner",
                    rating = 4.7f,
                    reviewCount = 35,
                    imageUrl = "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3"
                ),
                FoodResponse(
                    id = "3",
                    name = "Pancakes",
                    price = 12.0,
                    details = "Sweet pancakes with maple syrup and berries",
                    category = "Breakfast",
                    rating = 4.2f,
                    reviewCount = 15,
                    imageUrl = "https://images.unsplash.com/photo-1528207776546-365bb710ee93"
                ),
                FoodResponse(
                    id = "4",
                    name = "Salmon Salad",
                    price = 18.0,
                    details = "Fresh Atlantic salmon with green vegetables",
                    category = "Lunch",
                    rating = 4.8f,
                    reviewCount = 12,
                    imageUrl = "https://images.unsplash.com/photo-1467003909585-2f8a72700288"
                )
            )
        )
        /*
        return try {
            val response = api.getFoods()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        */
    }

    override suspend fun addFood(request: FoodRequest): Result<BaseResponse<FoodResponse>> {
        return try {
            val response = api.addFood(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoodById(id: String): Result<FoodResponse> {
        return try {
            val response = api.getFoodById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFood(id: String, request: FoodRequest): Result<BaseResponse<FoodResponse>> {
        return try {
            val response = api.updateFood(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFood(id: String): Result<BaseResponse<Unit>> {
        return try {
            val response = api.deleteFood(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
