package com.example.fooddelivery.ui.screens.restaurant.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.BestSellerItem
import com.example.fooddelivery.ui.screens.restaurant.component.dashboard.ActiveVouchersPreviewCard
import com.example.fooddelivery.ui.screens.restaurant.component.dashboard.BestSellerSection
import com.example.fooddelivery.ui.screens.restaurant.component.dashboard.HeaderSection
import com.example.fooddelivery.ui.screens.restaurant.component.dashboard.RevenueSection
import com.example.fooddelivery.ui.screens.restaurant.component.dashboard.ReviewSection
import com.example.fooddelivery.ui.screens.restaurant.component.dashboard.StatCard
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onSeeAllClick: () -> Unit = {},
    onSeeAllReviewsClick: (Int) -> Unit = {},
    onSeeRevenueClick: (Int) -> Unit = {},
    onAddFoodClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DashboardContent(
        state = state,
        onSeeAllClick = onSeeAllClick,
        onSeeAllReviewsClick = {
            state.restaurantId?.let { id ->
                onSeeAllReviewsClick(id)
            }
        },
        onSeeRevenueClick = {
            state.restaurantId?.let { id ->
                onSeeRevenueClick(id)
            }
        },
        onAddFoodClick = onAddFoodClick,
        onSeeAllOrdersClick = { onNavigate("order_management") },
        onNavigate = onNavigate
    )
}

@Composable
fun DashboardContent(
    state: DashboardState,
    onSeeAllClick: () -> Unit = {},
    onSeeAllReviewsClick: () -> Unit = {},
    onSeeRevenueClick: () -> Unit = {},
    onAddFoodClick: () -> Unit = {},
    onSeeAllOrdersClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                HeaderSection(
                    location = state.restaurantName.ifBlank { "My Restaurant" }
                )

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

                RevenueSection(
                    revenue = state.revenue,
                    onSeeDetailsClick = onSeeRevenueClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                ReviewSection(
                    state.rating,
                    state.totalReviews,
                    onSeeAllClicked = onSeeAllReviewsClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                OrderHistorySection(
                    orders = state.recentOrders,
                    totalOrders = state.totalOrders,
                    onSeeAllClick = onSeeAllOrdersClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                BestSellerSection(
                    items = state.bestSellers,
                    onSeeAllClick = onSeeAllClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                ActiveVouchersPreviewCard(
                    activeVouchers = state.activeVouchers,
                    vouchers = state.voucherPreviews,
                    onSeeDetailClick = {
                        onNavigate("coupons")
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun OrderHistorySection(
    orders: List<RecentOrder>,
    totalOrders: Int,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ORDER HISTORY",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = totalOrders.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Text(
                text = "See Details",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (orders.isEmpty()) {
                    Text(
                        text = "No recent orders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    orders.take(3).forEachIndexed { index, order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Order #${order.orderNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${order.customerName} • ${order.time}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${String.format("%.2f", order.totalPrice)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = order.status,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (order.status.equals("Delivered", true)) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                        if (index < orders.take(3).size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
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
                totalReviews = 20,
                totalOrders = 142,
                recentOrders = listOf(
                    RecentOrder("1", "9842", "Nguyen Van A", 24.50, "Delivered", "10:30 AM"),
                    RecentOrder("2", "9841", "Tran Thi B", 12.99, "Delivered", "09:15 AM"),
                    RecentOrder("3", "9840", "Le Van C", 45.00, "Cancelled", "Yesterday")
                ),
                bestSellers = listOf(
                    BestSellerItem("Burger", "$5.99", 4.5f, 120),
                    BestSellerItem("Pizza", "$8.99", 4.8f, 200)
                ),
                activeVouchers = 2,
                voucherPreviews = listOf(
                    VoucherPreviewItem("SUMMER25", "45/100 used"),
                    VoucherPreviewItem("FREESHIP", "212 used")
                )
            )
        )
    }
}