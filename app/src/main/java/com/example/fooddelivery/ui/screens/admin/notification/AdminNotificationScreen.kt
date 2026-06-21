package com.example.fooddelivery.ui.screens.admin.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.admin.components.VendorNotificationItem
import com.example.fooddelivery.ui.screens.admin.dashboard.RestaurantFormScreen
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminNotificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                DFoodTopBar(
                    title = "Notifications",
                    onBackClick = onNavigateBack
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            if (state.notifications.isEmpty() && !state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All Caught Up!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "There are no pending vendor registration requests waiting for your approval right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = state.notifications,
                        key = { it.id }
                    ) { item ->
                        VendorNotificationItem(
                            notification = item,
                            onApprove = {
                                val vendorId = item.id
                                viewModel.onEvent(AdminNotificationEvent.ApproveVendor(item.id, vendorId))
                            },
                            onReject = {
                                val vendorId = item.id
                                viewModel.onEvent(AdminNotificationEvent.RejectVendor(item.id, vendorId))
                            }
                        )
                    }

                    if (state.notifications.isNotEmpty()) {
                        item {
                            if (state.isPaginating) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                            } else if (!state.isEndReached) {
                                LaunchedEffect(true) {
                                    viewModel.onEvent(AdminNotificationEvent.LoadMore)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.isLoading && state.notifications.isEmpty() || state.isLoading && !state.isPaginating) {
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
fun AdminNotificationScreenReview() {
    DFoodTheme {
        AdminNotificationScreen(
            onNavigateBack = {}
        )
    }
}