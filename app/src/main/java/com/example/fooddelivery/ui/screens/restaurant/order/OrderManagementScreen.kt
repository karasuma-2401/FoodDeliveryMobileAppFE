package com.example.fooddelivery.ui.screens.restaurant.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.restaurant.component.OrderCard
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: OrderManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state
    val tabs = listOf("Requests", "Running", "History")

    val filteredOrders = remember(state.orders, state.selectedTab) {
        when (state.selectedTab) {
            0 -> state.orders.filter { it.status == OrderStatus.PENDING }
            1 -> state.orders.filter {
                it.status == OrderStatus.PREPARING || it.status == OrderStatus.DELIVERING
            }
            else -> state.orders.filter {
                // DELIVERED: nhà hàng đã giao, chờ khách confirm
                // CONFIRMED: khách xác nhận hoặc hệ thống auto-confirm sau 24h
                // CANCELLED: bị hủy
                it.status == OrderStatus.DELIVERED ||
                    it.status == OrderStatus.CONFIRMED ||
                    it.status == OrderStatus.CANCELLED
            }
        }
    }

    Scaffold(
        topBar = { DFoodTopBar(title = "Order Management", onBackClick = onNavigateBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = state.selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(text = title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No orders here", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredOrders) { order ->
                        OrderCard(
                            order = order,
                            isUpdating = state.updatingOrderId == order.id,
                            onAccept = { viewModel.acceptOrder(order.id) },
                            onDeny = { viewModel.denyOrder(order.id) },
                            onDone = { viewModel.completeOrder(order.id) },
                            onDelivered = { viewModel.deliverOrder(order.id) },
                            onCancel = { viewModel.cancelOrder(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OderManagementScreenPreview() {
    DFoodTheme {
        OrderManagementScreen(
            onNavigateBack = {}
        )
    }
}
