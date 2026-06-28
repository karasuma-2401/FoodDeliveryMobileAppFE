package com.example.fooddelivery.ui.screens.rating_reviews.restaurant_reviews

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.model.ReviewItem
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.ui.screens.rating_reviews.components.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewState(
    val reviews: List<ReviewItem> = emptyList(),
    val isLoading: Boolean = false,
    val currentRestaurantId: Int? = null,
    val userRole: UserRole = UserRole.CUSTOMER,
    val currentUserId: Int? = null,
    val errorMessage: String? = null
)

sealed interface ReviewEvent {
    data class LoadReviews(val restaurantId: Int) : ReviewEvent
    data class DeleteReview(val id: String) : ReviewEvent
    data class ReplyReview(val id: String, val replyText: String) : ReviewEvent
    data class EditReview(val review: ReviewItem) : ReviewEvent
    data object ErrorDismissed : ReviewEvent
}

sealed interface ReviewUiEffect {
    data class NavigateToEdit(
        val orderId: Int?,
        val restaurantId: Int,
        val restaurantName: String,
        val restaurantImage: String,
        val rating: Int,
        val comment: String,
        val reviewId: Int?,
        val tags: List<String> = emptyList(),
    ) : ReviewUiEffect
    data class ShowToast(val message: String) : ReviewUiEffect
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewState())
    val state = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ReviewUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        observeUserRole()
        observeUserId()
    }

    private fun observeUserRole() {
        viewModelScope.launch {
            tokenManager.getUserRoles.collect { roles ->
                val role = when {
                    roles.any { it.contains("ADMIN", ignoreCase = true) } -> UserRole.ADMIN
                    roles.any { it.contains("RESTAURANT", ignoreCase = true) || it.contains("BUSINESS", ignoreCase = true) } -> UserRole.BUSINESS
                    else -> UserRole.CUSTOMER
                }
                _state.update { it.copy(userRole = role) }
                Log.d("ReviewViewModel", "Current mapped role: $role from raw roles: $roles")
            }
        }
    }

    private fun observeUserId() {
        viewModelScope.launch {
            tokenManager.getUserId.collect { userId ->
                _state.update { it.copy(currentUserId = userId) }
                Log.d("ReviewViewModel", "Current user ID: $userId")
            }
        }
    }

    fun onEvent(event: ReviewEvent) {
        when (event) {
            is ReviewEvent.LoadReviews -> loadReviews(event.restaurantId)
            is ReviewEvent.DeleteReview -> deleteReview(event.id)
            is ReviewEvent.ReplyReview -> replyReview(event.id, event.replyText)
            is ReviewEvent.EditReview -> {
                viewModelScope.launch {
                    _state.value.currentRestaurantId?.let { resId ->
                        _uiEffect.emit(
                            ReviewUiEffect.NavigateToEdit(
                                orderId = event.review.orderId,
                                restaurantId = resId,
                                restaurantName = "",
                                restaurantImage = "",
                                rating = event.review.rating,
                                comment = event.review.description,
                                reviewId = event.review.id.toIntOrNull(),
                                tags = event.review.tags,
                            )
                        )
                    }
                }
            }
            is ReviewEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadReviews(restaurantId: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, currentRestaurantId = restaurantId) }
                restaurantRepository.getRestaurantReviews(restaurantId, limit = 20, offset = 0)
                    .onSuccess { reviews ->
                        val mapped = reviews.map { dto ->
                            ReviewItem(
                                id = dto.id.toString(),
                                userName = dto.user.name,
                                userAvatarUrl = dto.user.avatar,
                                userId = dto.user.id,
                                date = dto.createdAt,
                                title = dto.comment?.let { if (it.length > 50) it.take(50) + "..." else it } ?: "",
                                rating = dto.vote.coerceIn(1, 5),
                                description = dto.comment ?: "",
                                reply = dto.reply,
                                tags = dto.tags,
                                orderId = dto.orderId,
                            )
                        }
                        _state.update { it.copy(reviews = mapped, isLoading = false) }
                    }.onFailure { error ->
                        _state.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    private fun deleteReview(reviewId: String) {
        val id = reviewId.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            restaurantRepository.deleteReview(id).onSuccess {
                _state.value.currentRestaurantId?.let { loadReviews(it) }
                _uiEffect.emit(ReviewUiEffect.ShowToast("Review deleted successfully"))
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false) }
                _uiEffect.emit(ReviewUiEffect.ShowToast("Failed to delete review: ${error.localizedMessage}"))
            }
        }
    }

    private fun replyReview(reviewId: String, replyText: String) {
        val id = reviewId.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            restaurantRepository.replyReview(id, replyText).onSuccess {
                _state.value.currentRestaurantId?.let { loadReviews(it) }
                _uiEffect.emit(ReviewUiEffect.ShowToast("Replied successfully"))
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false) }
                _uiEffect.emit(ReviewUiEffect.ShowToast("Failed to reply: ${error.localizedMessage}"))
            }
        }
    }
}