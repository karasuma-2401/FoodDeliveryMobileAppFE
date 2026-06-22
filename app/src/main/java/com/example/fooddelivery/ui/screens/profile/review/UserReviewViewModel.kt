package com.example.fooddelivery.ui.screens.profile.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.UserReview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val page: Int = 1,
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
        val comment: String
    ) : UserReviewUiEffect
    data class ShowToast(val message: String) : UserReviewUiEffect
}

@HiltViewModel
class UserReviewViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(UserReviewState())
    val state = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<UserReviewUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private var fetchJob: Job? = null

    init {
        loadReviews()
    }

    fun onEvent(event: UserReviewEvent) {
        when (event) {
            is UserReviewEvent.RefreshReviews -> {
                fetchJob?.cancel()
                _state.update { it.copy(isRefreshing = true, page = 1, endReached = false) }
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
                        comment = event.review.comment
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
                
                delay(1000)

                val newPage = if (isRefresh) 1 else _state.value.page
                val mockPageData = generateMockReviews(newPage)

                _state.update { currentState ->
                    currentState.copy(
                        reviews = if (isRefresh) mockPageData else currentState.reviews + mockPageData,
                        isLoading = false,
                        isRefreshing = false,
                        page = newPage + 1,
                        endReached = mockPageData.isEmpty() || newPage >= 3,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false, 
                    isRefreshing = false, 
                    errorMessage = e.message ?: "An unexpected error occurred"
                ) }
            }
        }
    }

    private fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(500)
            _state.update { currentState ->
                currentState.copy(
                    reviews = currentState.reviews.filter { it.id != reviewId },
                    isLoading = false
                )
            }
            _uiEffect.emit(UserReviewUiEffect.ShowToast("Review deleted successfully"))
        }
    }

    private fun generateMockReviews(page: Int): List<UserReview> {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        
        return if (page > 3) emptyList() else listOf(
            UserReview(
                id = "rev_${page}_1",
                restaurantId = "res_1",
                restaurantName = "Pizza Hut",
                restaurantImage = "https://example.com/pizza.jpg",
                rating = 5,
                comment = "Best pizza ever! The crust was perfect and the toppings were fresh.",
                tags = listOf("Delicious Food", "Fast Delivery"),
                createdAt = now - (page * oneDay),
                orderId = "order_123"
            ),
            UserReview(
                id = "rev_${page}_2",
                restaurantId = "res_2",
                restaurantName = "Burger King",
                restaurantImage = "https://example.com/burger.jpg",
                rating = 3,
                comment = "Burger was okay, but the fries were a bit cold when they arrived.",
                tags = listOf("Good Value"),
                createdAt = now - (6 * oneDay),
                orderId = "order_456"
            )
        )
    }
}
