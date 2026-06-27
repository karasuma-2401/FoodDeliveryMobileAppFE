package com.example.fooddelivery.ui.screens.rating_reviews.restaurant_reviews

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.model.ReviewItem
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.ui.screens.rating_reviews.components.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewState(
    val reviews: List<ReviewItem> = emptyList(),
    val isLoading: Boolean = false,
    val currentRestaurantId: Int? = null,
    val userRole: UserRole = UserRole.CUSTOMER
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _state = mutableStateOf(ReviewState())
    val state: State<ReviewState> = _state

    init {
        observeUserRole()
    }

    private fun observeUserRole() {
        viewModelScope.launch {
            tokenManager.getUserRoles.collect { roles ->
                // Map từ List<String> sang Enum UserRole cho UI dễ xử lý
                val role = when {
                    roles.any { it.contains("ADMIN", ignoreCase = true) } -> UserRole.ADMIN
                    roles.any { it.contains("RESTAURANT", ignoreCase = true) || it.contains("BUSINESS", ignoreCase = true) } -> UserRole.BUSINESS
                    else -> UserRole.CUSTOMER
                }

                _state.value = _state.value.copy(userRole = role)
                Log.d("ReviewViewModel", "Current mapped role: $role from raw roles: $roles")
            }
        }
    }

    fun loadReviews(restaurantId: Int) {
        viewModelScope.launch {
            try {
                _state.value = state.value.copy(isLoading = true, currentRestaurantId = restaurantId)
                restaurantRepository.getRestaurantReviews(restaurantId, limit = 20, offset = 0)
                    .onSuccess { reviews ->
                        val mapped = reviews.map { dto ->
                            ReviewItem(
                                id = dto.id.toString(),
                                userName = dto.user.name,
                                userAvatarUrl = dto.user.avatar,
                                date = dto.createdAt,
                                title = dto.comment?.let { if (it.length > 50) it.take(50) + "..." else it } ?: "",
                                rating = dto.vote.coerceIn(1, 5),
                                description = dto.comment ?: "",
                                reply = dto.reply
                            )
                        }
                        _state.value = state.value.copy(reviews = mapped)
                    }.onFailure { error ->
                        Log.e("ReviewViewModel", "API Failure: ${error.localizedMessage}")
                    }
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Crash: ${e.localizedMessage}", e)
            } finally {
                _state.value = state.value.copy(isLoading = false)
            }
        }
    }

    fun deleteReview(reviewId: String) {
        val id = reviewId.toIntOrNull() ?: return
        viewModelScope.launch {
            restaurantRepository.deleteReview(id).onSuccess {
                state.value.currentRestaurantId?.let { loadReviews(it) }
            }.onFailure {
                Log.e("ReviewViewModel", "Delete Error: ${it.localizedMessage}")
            }
        }
    }

    fun replyReview(reviewId: String, replyText: String) {
        val id = reviewId.toIntOrNull() ?: return
        viewModelScope.launch {
            restaurantRepository.replyReview(id, replyText).onSuccess {
                state.value.currentRestaurantId?.let { loadReviews(it) }
            }.onFailure {
                Log.e("ReviewViewModel", "Reply Error: ${it.localizedMessage}")
            }
        }
    }
}