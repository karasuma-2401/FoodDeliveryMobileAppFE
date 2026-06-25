package com.example.fooddelivery.ui.screens.rating_reviews.restaurant_reviews

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.ReviewItem
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class ReviewState(
    val reviews: List<ReviewItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {
    private val _state = mutableStateOf(ReviewState())
    val state: State<ReviewState> = _state

    fun loadReviews(restaurantId: Int) {
        viewModelScope.launch {
            try {
                _state.value = state.value.copy(isLoading = true)
                restaurantRepository.getRestaurantReviews(restaurantId, limit = 20, offset = 0).onSuccess { reviews ->
                    val mapped = reviews.map { dto ->
                        ReviewItem(
                            id = dto.id.toString(),
                            userName = dto.user.name,
                            userAvatarUrl = dto.user.avatar,
                            date = dto.createdAt,
                            title = dto.comment?.let { if (it.length > 50) it.take(50) + "..." else it } ?: "",
                            rating = dto.vote.coerceIn(1, 5),
                            description = dto.comment ?: ""
                        )
                    }
                    _state.value = state.value.copy(reviews = mapped)
                }.onFailure { error ->
                    Log.e("ReviewViewModel", "API Failure: ${error.localizedMessage}")
                }
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Crash khi map data hoặc parse JSON: ${e.localizedMessage}", e)
            }
             finally {
                _state.value = state.value.copy(isLoading = false)
            }
        }
    }
}