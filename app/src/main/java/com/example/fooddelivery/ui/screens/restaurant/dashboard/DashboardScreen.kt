package com.example.fooddelivery.ui.screens.restaurant.dashboard
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.BestSellerItem
import com.example.fooddelivery.ui.screens.restaurant.component.BestSellerSection
import com.example.fooddelivery.ui.screens.restaurant.component.StatCard
import com.example.fooddelivery.ui.screens.restaurant.component.HeaderSection
import com.example.fooddelivery.ui.screens.restaurant.component.RevenueSection
import com.example.fooddelivery.ui.screens.restaurant.component.ReviewSection
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onSeeAllClick: () -> Unit = {},
    onSeeAllReviewsClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DashboardContent(
        state = state,
        onSeeAllClick = onSeeAllClick,
        onSeeAllReviewsClick = onSeeAllReviewsClick
    )
}

@Composable
fun DashboardContent(
    state: DashboardState,
    onSeeAllClick: () -> Unit = {},
    onSeeAllReviewsClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

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
        }
    }
}
@Preview (showBackground = true, showSystemUi = true)
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
