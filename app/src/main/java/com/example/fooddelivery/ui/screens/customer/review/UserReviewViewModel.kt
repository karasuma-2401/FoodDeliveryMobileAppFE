package com.example.fooddelivery.ui.screens.customer.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.UserReview
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserReviewState(
    val reviews: List<UserReview> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val offset: Int = 0,
    val endReached: Boolean = false,
    val errorMessage: String? = null
)

sealed interface UserReviewEvent {
    data object RefreshReviews : UserReviewEvent
    data object LoadNextPage : UserReviewEvent
    data class DeleteReview(val id: String) : UserReviewEvent
    data class EditReview(val review: UserReview) : UserReviewEvent
    data object ErrorDismissed : UserReviewEvent
}

sealed interface UserReviewUiEffect {
    data class NavigateToEdit(
        val orderId: String,
        val restaurantId: String,
        val restaurantName: String,
        val restaurantImage: String,
        val rating: Int,
        val comment: String,
        val reviewId: String,
        val tags: List<String> = emptyList(),
    ) : UserReviewUiEffect
    data class ShowToast(val message: String) : UserReviewUiEffect
}

@HiltViewModel
class UserReviewViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserReviewState())
    val state = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<UserReviewUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private var fetchJob: Job? = null
    private val limit = 20

    init {
        loadReviews()
    }

    fun onEvent(event: UserReviewEvent) {
        when (event) {
            is UserReviewEvent.RefreshReviews -> {
                fetchJob?.cancel()
                _state.update { it.copy(isRefreshing = true, offset = 0, endReached = false) }
                loadReviews(isRefresh = true)
            }
            is UserReviewEvent.LoadNextPage -> {
                loadReviews()
            }
            is UserReviewEvent.DeleteReview -> deleteReview(event.id)
            is UserReviewEvent.EditReview -> {
                viewModelScope.launch {
                    _uiEffect.emit(UserReviewUiEffect.NavigateToEdit(
                        orderId = event.review.orderId,
                        restaurantId = event.review.restaurantId,
                        restaurantName = event.review.restaurantName,
                        restaurantImage = event.review.restaurantImage,
                        rating = event.review.rating,
                        comment = event.review.comment,
                        reviewId = event.review.id,
                        tags = event.review.tags,
                    ))
                }
            }
            is UserReviewEvent.ErrorDismissed -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadReviews(isRefresh: Boolean = false) {
        if (!isRefresh && (_state.value.isLoading || _state.value.endReached)) return

        fetchJob = viewModelScope.launch {
            try {
                if (!isRefresh) _state.update { it.copy(isLoading = true) }
                
                val currentOffset = if (isRefresh) 0 else _state.value.offset
                
                userRepository.getUserReviews(limit = limit, offset = currentOffset)
                    .onSuccess { newReviews ->
                        // Merge new reviews
                        _state.update { currentState ->
                            currentState.copy(
                                reviews = if (isRefresh) newReviews else currentState.reviews + newReviews,
                                isLoading = false,
                                isRefreshing = false,
                                offset = currentOffset + newReviews.size,
                                endReached = newReviews.size < limit,
                                errorMessage = null
                            )
                        }

                        // For reviews missing restaurantImage, fetch restaurant details to fill image
                        val missingIds = newReviews
                            .filter { it.restaurantImage.isBlank() }
                            .mapNotNull { it.restaurantId.toIntOrNull() }
                            .toSet()

                        if (missingIds.isNotEmpty()) {
                            // Fetch details in parallel
                            missingIds.forEach { rid ->
                                viewModelScope.launch {
                                    restaurantRepository.getRestaurantById(rid)
                                        .onSuccess { resp ->
                                            val image = resp.image ?: resp.coverImage
                                            if (!image.isNullOrBlank()) {
                                                _state.update { cur ->
                                                    cur.copy(reviews = cur.reviews.map { r ->
                                                        if (r.restaurantId.toIntOrNull() == rid && r.restaurantImage.isBlank()) {
                                                            r.copy(restaurantImage = image)
                                                        } else r
                                                    })
                                                }
                                            }
                                        }
                                        .onFailure {
                                            // ignore failures silently
                                        }
                                }
                            }
                        }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(
                            isLoading = false, 
                            isRefreshing = false, 
                            errorMessage = e.message ?: "Could not load reviews"
                        ) }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false, 
                    isRefreshing = false, 
                    errorMessage = e.message ?: "An error occurred"
                ) }
            }
        }
    }

    private fun deleteReview(reviewId: String) {
        val idInt = reviewId.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            restaurantRepository.deleteReview(idInt)
                .onSuccess {
                    _state.update { currentState ->
                        currentState.copy(
                            reviews = currentState.reviews.filter { it.id != reviewId },
                            isLoading = false
                        )
                    }
                    _uiEffect.emit(UserReviewUiEffect.ShowToast("Review deleted successfully"))
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false) }
                    _uiEffect.emit(UserReviewUiEffect.ShowToast("Failed to delete review: ${e.message}"))
                }
        }
    }
}
