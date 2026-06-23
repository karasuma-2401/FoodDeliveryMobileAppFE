package com.example.fooddelivery.ui.screens.rating.restaurant_reviews

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.fooddelivery.domain.model.ReviewItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ReviewState(
    val reviews: List<ReviewItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ReviewViewModel @Inject constructor() : ViewModel() {
    private val _state = mutableStateOf(ReviewState())
    val state: State<ReviewState> = _state

    init {
        loadMockReviews()
    }

    private fun loadMockReviews() {
        _state.value = ReviewState(
            reviews = listOf(
                ReviewItem(
                    id = "1",
                    date = "20/12/2020",
                    title = "Great Food and Service",
                    rating = 5,
                    description = "This Food so tasty & delicious. Breakfast so fast Delivered in my place. Chef is very friendly. I'm really like chef for Home Food Order. Thanks."
                ),
                ReviewItem(
                    id = "2",
                    date = "20/12/2020",
                    title = "Awesome and Nice",
                    rating = 4,
                    description = "This Food so tasty & delicious. Breakfast so fast Delivered in my place."
                ),
                ReviewItem(
                    id = "3",
                    date = "20/12/2020",
                    title = "Awesome and Nice",
                    rating = 4,
                    description = "This Food so tasty & delicious."
                )
            )
        )
    }
}