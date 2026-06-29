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
import com.example.fooddelivery.ui.components.dialog.ConfirmDialogType
import com.example.fooddelivery.ui.components.dialog.DFoodConfirmDialog
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.restaurant.component.OrderCard
import com.example.fooddelivery.ui.theme.DFoodTheme

private enum class OrderConfirmAction {
    ACCEPT, DENY, CANCEL, DELIVER
}

private data class PendingOrderConfirm(
    val orderId: String,
    val action: OrderConfirmAction
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    onNavigateBack: () -> Unit,
    onChatWithCustomer: (orderId: Int, conversationId: Int?, customerName: String) -> Unit,
    viewModel: OrderManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state
    val tabs = listOf("Requests", "Running", "History")
    var pendingConfirm by remember { mutableStateOf<PendingOrderConfirm?>(null) }

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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                            onAccept = { pendingConfirm = PendingOrderConfirm(order.id, OrderConfirmAction.ACCEPT) },
                            onDeny = { pendingConfirm = PendingOrderConfirm(order.id, OrderConfirmAction.DENY) },
                            onDone = { viewModel.completeOrder(order.id) },
                            onDelivered = { pendingConfirm = PendingOrderConfirm(order.id, OrderConfirmAction.DELIVER) },
                            onCancel = { pendingConfirm = PendingOrderConfirm(order.id, OrderConfirmAction.CANCEL) },
                            onChatClick = {
                                onChatWithCustomer(
                                    order.id.toIntOrNull() ?: return@OrderCard,
                                    order.conversationId,
                                    order.customerName
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    pendingConfirm?.let { pending ->
        val dialog = when (pending.action) {
            OrderConfirmAction.ACCEPT -> OrderDialogContent(
                title = "Accept Order",
                message = "Accept order #${pending.orderId} and start preparing?",
                confirmText = "Accept",
                type = ConfirmDialogType.Default
            )
            OrderConfirmAction.DENY -> OrderDialogContent(
                title = "Deny Order",
                message = "Deny this order? The customer will be notified.",
                confirmText = "Deny",
                type = ConfirmDialogType.Destructive
            )
            OrderConfirmAction.CANCEL -> OrderDialogContent(
                title = "Cancel Order",
                message = "Cancel this order?",
                confirmText = "Cancel Order",
                type = ConfirmDialogType.Destructive
            )
            OrderConfirmAction.DELIVER -> OrderDialogContent(
                title = "Confirm Delivery",
                message = "Mark as delivered and confirm payment?",
                confirmText = "Confirm",
                type = ConfirmDialogType.Default
            )
        }

        DFoodConfirmDialog(
            title = dialog.title,
            message = dialog.message,
            confirmText = dialog.confirmText,
            type = dialog.type,
            isLoading = state.updatingOrderId == pending.orderId,
            onConfirm = {
                val orderId = pending.orderId
                pendingConfirm = null
                when (pending.action) {
                    OrderConfirmAction.ACCEPT -> viewModel.acceptOrder(orderId)
                    OrderConfirmAction.DENY -> viewModel.denyOrder(orderId)
                    OrderConfirmAction.CANCEL -> viewModel.cancelOrder(orderId)
                    OrderConfirmAction.DELIVER -> viewModel.deliverOrder(orderId)
                }
            },
            onDismiss = { pendingConfirm = null }
        )
    }
}

private data class OrderDialogContent(
    val title: String,
    val message: String,
    val confirmText: String,
    val type: ConfirmDialogType
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OderManagementScreenPreview() {
    DFoodTheme {
        OrderManagementScreen(
            onNavigateBack = {},
            onChatWithCustomer = { _, _, _ -> }
        )
    }
}
