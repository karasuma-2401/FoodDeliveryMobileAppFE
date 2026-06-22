package com.example.fooddelivery.ui.screens.rating

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.data.remote.dto.RestaurantRatingRequest
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

data class RatingReviewState(
    val orderId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val restaurantImage: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val isLoading: Boolean = false,
    val availableTags: List<String> = listOf(
        "Món ăn ngon", "Giao hàng nhanh", "Đóng gói cẩn thận", "Giá cả hợp lý", "Phục vụ tốt",
    ),
    val selectedTags: Set<String> = emptySet()
)

sealed interface RatingReviewEvent {
    object OnNavigateBack : RatingReviewEvent
    data class OnRatingChanged(val rating: Int) : RatingReviewEvent
    data class OnTagToggled(val tag: String) : RatingReviewEvent
    data class OnCommentChanged(val comment: String) : RatingReviewEvent
    object OnSubmit : RatingReviewEvent
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

    private val _state = MutableStateFlow(
        RatingReviewState(
            orderId = routeData.orderId,
            restaurantId = routeData.restaurantId,
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
        val currentState = _state.value
        if (currentState.isSubmitting) return

        if (currentState.rating == 0) {
            viewModelScope.launch {
                _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar("Vui lòng chọn số sao đánh giá"))
            }
            return
        }

        val orderIdInt = currentState.orderId.toIntOrNull() ?: return
        val restaurantIdInt = currentState.restaurantId.toIntOrNull() ?: 0

        viewModelScope.launch {
            try {
                _state.update { it.copy(isSubmitting = true) }
                
                val request = RestaurantRatingRequest(
                    restaurantId = restaurantIdInt,
                    orderId = orderIdInt,
                    vote = currentState.rating,
                    comment = currentState.comment,
                    tags = currentState.selectedTags.toList()
                )

                restaurantRepository.rateRestaurant(request).onSuccess {
                    _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar("Đánh giá đã được gửi thành công!"))
                    _uiEffect.emit(RatingReviewUiEffect.NavigateBack)
                }.onFailure { e ->
                    _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar("Gửi đánh giá thất bại: ${e.message}"))
                }

            } catch (e: Exception) {
                _uiEffect.emit(RatingReviewUiEffect.ShowSnackBar(e.localizedMessage ?: "Đã có lỗi xảy ra"))
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }
}
