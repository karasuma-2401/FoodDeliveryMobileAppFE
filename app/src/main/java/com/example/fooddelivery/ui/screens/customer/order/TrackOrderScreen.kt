package com.example.fooddelivery.ui.screens.customer.order

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.customer.checkout.components.SectionTitle
import com.example.fooddelivery.ui.screens.customer.order.components.DeliveryAddressCard
import com.example.fooddelivery.ui.screens.customer.order.components.OrderBillDetailCard
import com.example.fooddelivery.ui.screens.customer.order.components.OrderSummaryCard
import com.example.fooddelivery.ui.screens.customer.order.components.RestaurantContactCard
import com.example.fooddelivery.ui.screens.customer.order.components.OrderStatusHeroCard
import com.example.fooddelivery.ui.screens.customer.order.components.TimelineItem
import com.example.fooddelivery.ui.screens.customer.order.components.TrackOrderSkeleton
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.theme.CustomerDimens
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOrderScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onChatWithRestaurant: (Int, Int, String, String) -> Unit,
    viewModel: TrackOrderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(orderId) {
        viewModel.onEvent(TrackOrderEvent.Initialize(orderId))
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                TrackOrderUiEffect.ConfirmReceivedSuccess -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    snackBarHostState.showSnackbar(
                        message = "Order completed. Thanks!",
                        duration = SnackbarDuration.Short
                    )
                }
                is TrackOrderUiEffect.ShowSnackBar -> {
                    snackBarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    TrackOrderContent(
        state = state,
        snackBarHostState = snackBarHostState,
        onNavigateBack = onNavigateBack,
        onChatWithRestaurant = onChatWithRestaurant,
        onConfirmReceived = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onEvent(TrackOrderEvent.ConfirmReceived)
        },
        onCheckPayment = { viewModel.onEvent(TrackOrderEvent.CheckPaymentStatus) },
        onRefresh = { viewModel.onEvent(TrackOrderEvent.Refresh) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOrderContent(
    state: TrackOrderState,
    onNavigateBack: () -> Unit,
    onChatWithRestaurant: (Int, Int, String, String) -> Unit,
    onConfirmReceived: () -> Unit = {},
    onCheckPayment: () -> Unit = {},
    onRefresh: () -> Unit = {},
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val context = LocalContext.current
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            DFoodTopBar(
                title = if (state.orderId.isNotEmpty()) "Order #${state.orderId}" else "Order Details",
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading && state.orderDetail == null) {
            Box(modifier = Modifier.padding(innerPadding)) {
                TrackOrderSkeleton()
            }
        } else {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = CustomerDimens.screenHorizontalPadding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(CustomerDimens.screenSectionSpacing)
                ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Payment Pending Notice for MoMo
                if (state.paymentMethod == "MOMO" && state.paymentStatus != "DONE" && state.trackingStatus != TrackingStatus.CANCELLED) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), // Light Amber
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFFFFA000))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Waiting for payment confirmation...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF827717)
                                )
                            }
                            IconButton(onClick = onCheckPayment) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh Payment", tint = Color(0xFFFFA000))
                            }
                        }
                    }
                }

                // Status Banner for Terminal States
                AnimatedVisibility(visible = state.trackingStatus == TrackingStatus.CANCELLED) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "This order has been cancelled.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Hero status card (hidden when cancelled — dedicated banner below)
                if (state.trackingStatus != TrackingStatus.CANCELLED) {
                    AnimatedContent(
                        targetState = state.trackingStatus,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "order_status_hero",
                    ) { status ->
                        OrderStatusHeroCard(
                            trackingStatus = status,
                            expectedArrivalDisplay = state.expectedArrivalDisplay,
                            deliveredAtDisplay = state.deliveredAtDisplay,
                            countdownLabel = state.countdownLabel,
                        )
                    }
                }

                // Tracking Section
                if (state.trackingStatus != TrackingStatus.CANCELLED) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionTitle(
                            title = "Live Tracking",
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        val stepsToShow = TrackingStatus.entries.filter { it.step >= 0 }
                        stepsToShow.forEachIndexed { index, status ->
                            val isCompleted = state.trackingStatus.step > status.step
                            val isActive = state.trackingStatus == status

                            TimelineItem(
                                title = status.title,
                                subtitle = status.subtitle,
                                icon = getTrackingIcon(status),
                                isCompleted = isCompleted,
                                isActive = isActive,
                                isLast = index == stepsToShow.size - 1
                            )
                        }
                    }
                }

                // Confirm Received Action
                if (state.shouldShowConfirmReceivedAction) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Have you received your order?",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            state.hoursUntilAutoConfirm?.let { hours ->
                                Text(
                                    text = "Order will be auto-confirmed in ${hours.toInt()} hours.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onConfirmReceived,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isConfirming,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (state.isConfirming) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Confirm Received")
                                }
                            }
                        }
                    }
                }

                // Contact Card
                RestaurantContactCard(
                    restaurantName = state.restaurantName,
                    restaurantImage = state.restaurantImage,
                    onCallClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${state.restaurantPhone}")
                        }
                        context.startActivity(intent)
                    },
                    onChatClick = {
                        onChatWithRestaurant(
                            state.orderId.toIntOrNull() ?: 0,
                            state.sellerId,
                            state.restaurantName,
                            state.restaurantImage
                        )
                    }
                )

                // Delivery Address
                SectionTitle(title = "Delivery Details")
                DeliveryAddressCard(address = state.address)

                // Order Summary
                OrderSummaryCard(items = state.items)

                // Payment & Billing Detail
                OrderBillDetailCard(
                    totalPrice = state.totalPrice,
                    paymentMethod = state.paymentMethod,
                    paymentStatus = state.paymentStatus,
                    paymentDate = state.orderDetail?.paymentDate,
                    voucherInfo = state.voucherInfo
                )

                // Order Note
                if (!state.note.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Order Note",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun getTrackingIcon(status: TrackingStatus): ImageVector {
    return when (status) {
        TrackingStatus.PENDING -> Icons.Default.Receipt
        TrackingStatus.PREPARING -> Icons.Default.RestaurantMenu
        TrackingStatus.DELIVERING -> Icons.Default.DirectionsBike
        TrackingStatus.DELIVERED -> Icons.Default.ThumbUp
        TrackingStatus.CONFIRMED -> Icons.Default.CheckCircle
        TrackingStatus.CANCELLED -> Icons.Default.Cancel
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TrackOrderContentPreview() {
    DFoodTheme(darkTheme = false) {
        TrackOrderContent(
            state = TrackOrderState(
                orderId = "162432",
                trackingStatus = TrackingStatus.PREPARING,
                restaurantName = "Rose Garden Restaurant",
                restaurantPhone = "0987654321",
                items = listOf(
                    OrderSummaryItem("Burger Bistro", 1, 15.0, "Extra cheese", ""),
                    OrderSummaryItem("Garden Pizza", 1, 22.0, "Medium size", "")
                ),
                totalPrice = 37.0,
                paymentMethod = "MOMO",
                paymentStatus = "DONE"
            ),
            onNavigateBack = {},
            onChatWithRestaurant = { _, _, _, _ -> }
        )
    }
}
