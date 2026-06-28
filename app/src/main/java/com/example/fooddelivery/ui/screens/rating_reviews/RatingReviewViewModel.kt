package com.example.fooddelivery.ui.screens.rating_reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.data.remote.dto.RestaurantRatingRequest
import com.example.fooddelivery.data.remote.dto.UpdateReviewRequest
import com.example.fooddelivery.data.remote.dto.VendorReviewResponse
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.ui.navigation.RatingReviewRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val DEFAULT_REVIEW_TAG_LABELS = listOf(
    "Delicious food",
    "Fast delivery",
    "Careful packaging",
    "Friendly attitude",
    "Reasonable price",
)

private const val REVIEW_TAGS_DISPLAY_LIMIT = 5

private fun buildAvailableTags(preselected: Set<String>): List<String> {
    val defaults = DEFAULT_REVIEW_TAG_LABELS.take(REVIEW_TAGS_DISPLAY_LIMIT)
    val extra = preselected.filter { it.isNotBlank() && it !in defaults }
    return (defaults + extra).distinct()
}

data class RatingReviewState(
    val orderId: String = "",
    val restaurantId: String = "",
    val reviewId: String? = null,
    val restaurantName: String = "",
    val restaurantImage: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val isLoading: Boolean = false,
    val availableTags: List<String> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val reviews: List<VendorReviewResponse> = emptyList()
)

sealed interface RatingReviewEvent {
    object OnNavigateBack : RatingReviewEvent
    data class OnRatingChanged(val rating: Int) : RatingReviewEvent
    data class OnTagToggled(val tag: String) : RatingReviewEvent
    data class OnCommentChanged(val comment: String) : RatingReviewEvent
    object OnSubmit : RatingReviewEvent
    object OnDelete : RatingReviewEvent
}

sealed interface RatingReviewUiEffect {
    data class ShowSnackBar(val message: String) : RatingReviewUiEffect
    object NavigateBack : RatingReviewUiEffect
}

@HiltViewModel
class RatingReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {
    private val routeData = savedStateHandle.toRoute<RatingReviewRoute>()
    private val initialTags = routeData.initialTags.filter { it.isNotBlank() }.toSet()

    private val _state = MutableStateFlow(
        RatingReviewState(
            orderId = routeData.orderId,
            restaurantId = routeData.restaurantId,
            reviewId = routeData.reviewId,
            restaurantName = routeData.restaurantName,
            restaurantImage = routeData.restaurantImage,
            rating = routeData.initialRating,
            comment = routeData.initialComment,
            availableTags = buildAvailableTags(initialTags),
            selectedTags = initialTags,
        )
    )
    val state: StateFlow<RatingReviewState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<RatingReviewUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadReviews()
    }

    private fun loadReviews() {
        val restaurantIdInt = routeData.restaurantId.toIntOrNull() ?: return
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                restaurantRepository.getRestaurantReviews(restaurantIdInt).onSuccess { reviews ->
                    _state.update { it.copy(reviews = reviews) }
                }.onFailure { e ->
                    _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar("Failed to load reviews: ${e.message}"))
                }
            } catch (e: Exception) {
                _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar(e.localizedMessage ?: "An error occurred while loading reviews"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: RatingReviewEvent) {
        when (event) {
            is RatingReviewEvent.OnNavigateBack -> {
                viewModelScope.launch { _uiEffect.emit(RatingReviewUiEffect.NavigateBack) }
            }
            is RatingReviewEvent.OnRatingChanged -> {
                _state.update { it.copy(rating = event.rating.coerceIn(0, 5)) }
            }
            is RatingReviewEvent.OnTagToggled -> {
                _state.update {
                    val newTags = if (it.selectedTags.contains(event.tag)) it.selectedTags - event.tag else it.selectedTags + event.tag
                    it.copy(selectedTags = newTags)
                }
            }
            is RatingReviewEvent.OnCommentChanged -> {
                _state.update { it.copy(comment = event.comment) }
            }
            RatingReviewEvent.OnSubmit -> submitReview()
            RatingReviewEvent.OnDelete -> deleteReview()
        }
    }

    private fun submitReview() {
        val currentState = _state.value
        if (currentState.isSubmitting) return

        if (currentState.rating == 0) {
            viewModelScope.launch {
                _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar("Please select a star rating"))
            }
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isSubmitting = true) }
                
                val result = if (currentState.reviewId != null) {
                    // Update existing review
                    val updateRequest = UpdateReviewRequest(
                        vote = currentState.rating,
                        comment = currentState.comment,
                        tags = currentState.selectedTags.toApiTagValues()
                    )
                    restaurantRepository.updateReview(currentState.reviewId.toInt(), updateRequest)
                } else {
                    // Create new review
                    val orderIdInt = currentState.orderId.toIntOrNull() ?: return@launch
                    val restaurantIdInt = currentState.restaurantId.toIntOrNull() ?: 0
                    val createRequest = RestaurantRatingRequest(
                        orderId = orderIdInt,
                        vote = currentState.rating,
                        comment = currentState.comment,
                        tags = currentState.selectedTags.toApiTagValues()
                    )
                    restaurantRepository.rateRestaurant(restaurantIdInt, createRequest)
                }

                result.onSuccess {
                    val successMsg = if (currentState.reviewId != null) "Review updated successfully!" else "Review submitted successfully!"
                    _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar(successMsg))
                    _uiEffect.emit(RatingReviewUiEffect.NavigateBack)
                }.onFailure { e ->
                    _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar("Operation failed: ${e.message}"))
                }

            } catch (e: Exception) {
                _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar(e.localizedMessage ?: "An error occurred"))
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun deleteReview() {
        val currentState = _state.value
        val reviewIdInt = currentState.reviewId?.toIntOrNull() ?: return

        viewModelScope.launch {
            try {
                _state.update { it.copy(isSubmitting = true) }
                restaurantRepository.deleteReview(reviewIdInt).onSuccess {
                    _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar("Review deleted successfully!"))
                    _uiEffect.emit(RatingReviewUiEffect.NavigateBack)
                }.onFailure { e ->
                    _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar("Failed to delete review: ${e.message}"))
                }
            } catch (e: Exception) {
                _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar(e.localizedMessage ?: "An error occurred"))
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun Set<String>.toApiTagValues(): List<String> {
        // Backend expects exact allowed labels (not snake_case).
        return this.filter { it.isNotBlank() }
    }
}
