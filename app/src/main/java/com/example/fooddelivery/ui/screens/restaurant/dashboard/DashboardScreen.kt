package com.example.fooddelivery.ui.screens.restaurant.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.BestSellerItem
import com.example.fooddelivery.ui.screens.restaurant.component.*
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onSeeAllClick: () -> Unit = {},
    onSeeAllReviewsClick: () -> Unit = {},
    onAddFoodClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DashboardContent(
        state = state,
        onSeeAllClick = onSeeAllClick,
        onSeeAllReviewsClick = onSeeAllReviewsClick,
        onAddFoodClick = onAddFoodClick,
        onNavigate = onNavigate
    )
}

@Composable
fun DashboardContent(
    state: DashboardState,
    onSeeAllClick: () -> Unit = {},
    onSeeAllReviewsClick: () -> Unit = {},
    onAddFoodClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DFoodBottomBar(
                currentRoute = "dashboard",
                onNavigate = onNavigate,
                onAddClick = onAddFoodClick
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.error, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                HeaderSection("TP HCM")

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "RUNNING ORDERS",
                        value = state.runningOrders,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "ORDER REQUEST",
                        value = state.orderRequest,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                RevenueSection(state.revenue)

                Spacer(modifier = Modifier.height(20.dp))

                ReviewSection(state.rating, state.totalReviews, onSeeAllClicked = onSeeAllReviewsClick)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                BestSellerSection(
                    items = listOf(
                        BestSellerItem("Burger", "$5.99", 4.5f, 120, R.drawable.ic_launcher_background),
                        BestSellerItem("Pizza", "$8.99", 4.8f, 200, R.drawable.ic_launcher_background),
                        BestSellerItem("Chicken", "$6.49", 4.6f, 150, R.drawable.ic_launcher_background)
                    ),
                    onSeeAllClick = onSeeAllClick
                )
                ActiveVouchersPreviewCard(
                    onSeeDetailClick = {
                        onNavigate("coupons")
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    DFoodTheme {
        DashboardContent(
            state = DashboardState(
                runningOrders = 20,
                orderRequest = 5,
                revenue = 2241.0,
                rating = 4.9,
                totalReviews = 20
            )
        )
    }
}
