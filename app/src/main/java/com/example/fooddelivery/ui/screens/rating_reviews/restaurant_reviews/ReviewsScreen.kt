package com.example.fooddelivery.ui.screens.rating_reviews.restaurant_reviews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.rating_reviews.components.ReviewItemRow
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    restaurantId: Int,
    onNavigateBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state
    var reviewIdToReply by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = restaurantId) {
        viewModel.loadReviews(restaurantId)
    }

    // Hiển thị Dialog khi có ID review cần reply
    if (reviewIdToReply != null) {
        ReplyDialog(
            onDismiss = { reviewIdToReply = null },
            onSubmit = { replyText ->
                reviewIdToReply?.let { id ->
                    viewModel.replyReview(id, replyText)
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
                        onEditClick = {
                            // Xử lý logic Edit nếu cần
                        },
                        onDeleteClick = {
                            viewModel.deleteReview(review.id)
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
        ReviewScreen(restaurantId = 1, onNavigateBack = {})
    }
}