package com.example.fooddelivery.ui.screens.rating_reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.layout.NavigationBarBottomSpacer
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.rating_reviews.components.ReviewTagCloud
import com.example.fooddelivery.ui.screens.rating_reviews.components.StarRatingBar
import com.example.fooddelivery.ui.theme.DFoodTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RatingReviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: RatingReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is RatingReviewUiEffect.ShowSnackBar -> {
                    snackBarHostState.showSnackbar(effect.message)
                }

                is RatingReviewUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }
    RatingReviewContent(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHost = snackBarHostState,
        modifier = Modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingReviewContent(
    state: RatingReviewState,
    onEvent: (RatingReviewEvent) -> Unit,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            DFoodTopBar(
                title = if (state.reviewId != null) "Edit Review" else "Rating & Review",
                onBackClick = { onEvent(RatingReviewEvent.OnNavigateBack) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHost) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = state.restaurantImage,
                            contentDescription = state.restaurantName,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = state.restaurantName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "How was your meal?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        StarRatingBar(
                            rating = state.rating,
                            onRRatingChanged = { onEvent(RatingReviewEvent.OnRatingChanged(it)) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        ReviewTagCloud(
                            availableTags = state.availableTags,
                            selectedTags = state.selectedTags,
                            onTagToggled = { onEvent(RatingReviewEvent.OnTagToggled(it)) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = state.comment,
                            onValueChange = { onEvent(RatingReviewEvent.OnCommentChanged(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            placeholder = {
                                Text(
                                    text = "Write your detailed experience here...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                DFoodButton(
                    text = if (state.reviewId != null) "Update Review" else "Submit Review",
                    onClick = { onEvent(RatingReviewEvent.OnSubmit) },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = state.isSubmitting,
                    enabled = !state.isSubmitting
                )
                if (state.reviewId != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = { onEvent(RatingReviewEvent.OnDelete) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        enabled = !state.isSubmitting
                    ) {
                        Text("Delete Review")
                    }
                }
                NavigationBarBottomSpacer()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RatingReviewScreenPreview() {
    DFoodTheme(darkTheme = false) {
        RatingReviewContent(
            state = RatingReviewState(),
            onEvent = {},
            snackbarHost = remember { SnackbarHostState() }
        )
    }
}
