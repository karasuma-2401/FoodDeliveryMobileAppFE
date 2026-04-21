package com.example.fooddelivery.ui.screens.vendor.dashboard
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.domain.model.BestSellerItem
import com.example.fooddelivery.ui.components.BestSeller.BestSellerSection
import com.example.fooddelivery.ui.components.card.StatCard
import com.example.fooddelivery.ui.components.header.HeaderSection
import com.example.fooddelivery.ui.components.revenue.RevenueSection
import com.example.fooddelivery.ui.components.review.ReviewSection
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state

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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("RUNNING ORDERS", state.runningOrders)
                StatCard("ORDER REQUEST", state.orderRequest)
            }

            Spacer(modifier = Modifier.height(20.dp))

            RevenueSection(state.revenue)

            Spacer(modifier = Modifier.height(20.dp))

            ReviewSection(state.rating, state.totalReviews)
            Spacer(modifier = Modifier.height(20.dp))
            BestSellerSection(
                items = listOf(
                    BestSellerItem("Burger", "$5.99", 4.5f, 120, R.drawable.ic_launcher_background),
                    BestSellerItem("Pizza", "$8.99", 4.8f, 200, R.drawable.ic_launcher_background),
                    BestSellerItem("Chicken", "$6.49", 4.6f, 150, R.drawable.ic_launcher_background)
                ),
                onSeeAllClick = {
                }
            )
        }
    }
}
@Preview (showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    DFoodTheme {
        DashboardScreen()
    }
}