package com.example.fooddelivery.ui.screens.rating

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.ui.navigation.RatingReviewRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RatingReviewState (
    val orderId: String = "",
    val restaurantName: String = "",
    val restaurantImage: String = "",
    val rating: Int = 0,
    val selectedTags: Set<String> = emptySet(),
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val availableTags: List<String> = listOf(
        "Delicious Food", "Great Delivery", "Fast Delivery", "Food Delivery", "Good Value",
    )
)

sealed interface RatingReviewEvent {
    object OnNavigateBack: RatingReviewEvent
    data class OnRatingChanged(val rating: Int): RatingReviewEvent
    data class OnTagToggled(val tag: String): RatingReviewEvent
    data class OnCommentChanged(val comment: String): RatingReviewEvent
    object OnSubmit: RatingReviewEvent
}

sealed interface RatingReviewUiEffect {
    data class ShowSnackBar (val message: String): RatingReviewUiEffect
    object NavigateBack: RatingReviewUiEffect
}

@HiltViewModel
class RatingReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val routeData = savedStateHandle.toRoute<RatingReviewRoute>()
    
    private val _state = MutableStateFlow(
        RatingReviewState(
            orderId = routeData.orderId,
            restaurantName = routeData.restaurantName,
            restaurantImage = routeData.restaurantImage,
            rating = routeData.initialRating,
            comment = routeData.initialComment
        )
    )
    val state: StateFlow<RatingReviewState> = _state.asStateFlow()
    
    private val _uiEffect = MutableSharedFlow<RatingReviewUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

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
        }
    }

    private fun submitReview() {
        if (_state.value.isSubmitting) return
        viewModelScope.launch {
            try {
                _state.update { it.copy(isSubmitting = true) }
                delay(1000)
                _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar("Success!"))
                _uiEffect.emit(RatingReviewUiEffect.NavigateBack)
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }
}
