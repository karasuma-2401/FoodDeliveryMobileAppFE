package com.example.fooddelivery.ui.screens.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.order.components.OrderSummaryCard
import com.example.fooddelivery.ui.screens.order.components.RestaurantContactCard
import com.example.fooddelivery.ui.screens.order.components.TimelineItem
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOrderScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onChatWithRestaurant: (Int, Int, String, String) -> Unit,
    viewModel: TrackOrderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.onEvent(TrackOrderEvent.Initialize(orderId))
    }
    TrackOrderContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onChatWithRestaurant = onChatWithRestaurant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOrderContent(
    state: TrackOrderState,
    onNavigateBack: () -> Unit,
    onChatWithRestaurant: (Int, Int, String, String) -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Order #${state.orderId}",
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EXPECTED ARRIVAL",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.expectedArrival,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Live Tracking",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val statuses = TrackingStatus.entries
                statuses.forEachIndexed { index, status ->
                    val isCompleted = state.trackingStatus.step > status.step || (state.trackingStatus == status && state.trackingStatus == TrackingStatus.COMPLETED)
                    val isActive = state.trackingStatus == status

                    TimelineItem(
                        title = status.title,
                        subtitle = status.subtitle,
                        icon = getTrackingIcon(status),
                        isCompleted = isCompleted,
                        isActive = isActive,
                        isLast = index == statuses.size - 1
                    )
                }
            }
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
                        state.restaurantId,
                        state.restaurantName,
                        state.restaurantImage
                    ) 
                }
            )
            OrderSummaryCard(items = state.items)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun getTrackingIcon(status: TrackingStatus): ImageVector {
    return when (status) {
        TrackingStatus.PENDING -> Icons.Default.Receipt
        TrackingStatus.CONFIRMED -> Icons.Default.ThumbUp
        TrackingStatus.PREPARING -> Icons.Default.RestaurantMenu
        TrackingStatus.DELIVERING -> Icons.Default.DirectionsBike
        TrackingStatus.COMPLETED -> Icons.Default.CheckCircle
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TrackOrderContentPreview() {
    DFoodTheme(darkTheme = false) {
        TrackOrderContent(
            state = TrackOrderState(
                orderId = "162432",
                expectedArrival = "12:45 PM",
                trackingStatus = TrackingStatus.PREPARING,
                restaurantName = "Rose Garden Restaurant",
                restaurantPhone = "0987654321",
                items = listOf(
                    OrderSummaryItem("Burger Bistro", 1, "Extra cheese", ""),
                    OrderSummaryItem("Garden Pizza", 1, "Medium size", "")
                )
            ),
            onNavigateBack = {},
            onChatWithRestaurant = { _, _, _, _ -> }
        )
    }
}
