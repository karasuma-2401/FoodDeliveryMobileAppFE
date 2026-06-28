package com.example.fooddelivery.ui.screens.rating_reviews.restaurant_reviews

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.rating_reviews.components.ReviewItemRow
import com.example.fooddelivery.ui.theme.DFoodTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    restaurantId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (
        orderId: Int?,
        restaurantId: Int,
        name: String,
        image: String,
        rating: Int,
        comment: String,
        reviewId: Int?,
        tags: List<String>,
    ) -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var reviewIdToReply by remember { mutableStateOf<String?>(null) }
    var reviewIdToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = restaurantId) {
        viewModel.onEvent(ReviewEvent.LoadReviews(restaurantId))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ReviewUiEffect.NavigateToEdit -> {
                    onNavigateToEdit(
                        effect.orderId,
                        effect.restaurantId,
                        effect.restaurantName,
                        effect.restaurantImage,
                        effect.rating,
                        effect.comment,
                        effect.reviewId,
                        effect.tags,
                    )
                }
                is ReviewUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (reviewIdToDelete != null) {
        AlertDialog(
            onDismissRequest = { reviewIdToDelete = null },
            title = { Text("Delete Review", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this review? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        reviewIdToDelete?.let { viewModel.onEvent(ReviewEvent.DeleteReview(it)) }
                        reviewIdToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewIdToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (reviewIdToReply != null) {
        ReplyDialog(
            onDismiss = { reviewIdToReply = null },
            onSubmit = { replyText ->
                reviewIdToReply?.let { id ->
                    viewModel.onEvent(ReviewEvent.ReplyReview(id, replyText))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Reviews",
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No reviews yet for this restaurant.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(
                    items = state.reviews,
                    key = { it.id }
                ) { review ->
                    ReviewItemRow(
                        review = review,
                        userRole = state.userRole,
                        currentUserId = state.currentUserId,
                        onEditClick = {
                            viewModel.onEvent(ReviewEvent.EditReview(review))
                        },
                        onDeleteClick = {
                            reviewIdToDelete = review.id
                        },
                        onReplyClick = {
                            reviewIdToReply = review.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReplyDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Reply to Review") },
        text = {
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                label = { Text("Your response") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (replyText.isNotBlank()) onSubmit(replyText)
                    onDismiss()
                }
            ) { Text("Send") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReviewScreenPreview() {
    DFoodTheme {
        ReviewScreen(
            restaurantId = 1,
            onNavigateBack = {},
            onNavigateToEdit = { _, _, _, _, _, _, _, _ -> }
        )
    }
}