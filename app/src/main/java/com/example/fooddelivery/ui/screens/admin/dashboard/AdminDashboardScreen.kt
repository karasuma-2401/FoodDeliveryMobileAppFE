package com.example.fooddelivery.ui.screens.admin.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.screens.admin.components.DashboardRevenueCard
import com.example.fooddelivery.ui.screens.admin.components.DashboardStatsGrid
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun AdminDashboardScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AdminDashboardContent(
        state = state,
        onEvent = { event -> viewModel.onEvent(event) },
        onNavigate = onNavigate,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardContent(
    state: AdminDashboardState,
    onEvent: (AdminDashboardEvent) -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { onEvent(AdminDashboardEvent.Refresh) }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardRevenueCard(revenue = state.stats.deliveredRevenue)

                Text(
                    text = "System Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )

                DashboardStatsGrid(
                    stats = state.stats,
                    onUsersClick = { onNavigate("users") },
                    onRestaurantsClick = { onNavigate("restaurants") },
                    onOrdersClick = { onNavigate("orders") },
                    onPaymentsClick = { onNavigate("payments") },
                    onCategoriesClick = { onNavigate("categories") },
                    onVouchersClick = { onNavigate("coupons") }
                )
            }
        }

        if (state.isLoading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminDashboardScreenReview() {
    DFoodTheme {
        AdminDashboardContent(
            state = AdminDashboardState(
                stats = DashboardStats(
                    users = 150,
                    restaurants = 12,
                    orders = 450,
                    payments = 400,
                    categories = 8,
                    vouchers = 15,
                    deliveredRevenue = 75000000.0
                ),
                isLoading = false
            ),
            onEvent = {},
            onNavigate = {}
        )
    }
}