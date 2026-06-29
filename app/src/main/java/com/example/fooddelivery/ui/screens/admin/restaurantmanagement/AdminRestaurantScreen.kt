package com.example.fooddelivery.ui.screens.admin.restaurantmanagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.ui.components.dialog.ConfirmDialogType
import com.example.fooddelivery.ui.components.dialog.DFoodConfirmDialog
import com.example.fooddelivery.ui.screens.admin.components.RestaurantItemRow

private enum class RestaurantApprovalAction { APPROVE, REJECT }

private data class PendingRestaurantApproval(
    val restaurantId: Int,
    val restaurantName: String,
    val action: RestaurantApprovalAction
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRestaurantScreen(
    viewModel: AdminRestaurantViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToRestaurantDetail: (Int) -> Unit
) {
    val state by viewModel.state
    var pendingApproval by remember { mutableStateOf<PendingRestaurantApproval?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Review Restaurants", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.restaurants, key = { it.id }) { restaurant ->
                        RestaurantItemRow(
                            name = restaurant.name,
                            phone = restaurant.phone,
                            status = restaurant.status,
                            image = restaurant.image,
                            onApprove = {
                                pendingApproval = PendingRestaurantApproval(
                                    restaurantId = restaurant.id,
                                    restaurantName = restaurant.name,
                                    action = RestaurantApprovalAction.APPROVE
                                )
                            },
                            onReject = {
                                pendingApproval = PendingRestaurantApproval(
                                    restaurantId = restaurant.id,
                                    restaurantName = restaurant.name,
                                    action = RestaurantApprovalAction.REJECT
                                )
                            },
                            onClick = { onNavigateToRestaurantDetail(restaurant.id) }
                        )
                    }
                }
            }
        }
    }

    pendingApproval?.let { pending ->
        val isApprove = pending.action == RestaurantApprovalAction.APPROVE
        DFoodConfirmDialog(
            title = if (isApprove) "Approve Restaurant" else "Reject Restaurant",
            message = if (isApprove) {
                "Approve \"${pending.restaurantName}\" as a partner?"
            } else {
                "Reject \"${pending.restaurantName}\" registration?"
            },
            confirmText = if (isApprove) "Approve" else "Reject",
            type = if (isApprove) ConfirmDialogType.Default else ConfirmDialogType.Destructive,
            onConfirm = {
                val status = if (isApprove) "APPROVED" else "REJECTED"
                viewModel.updateApprovalStatus(pending.restaurantId, status)
                pendingApproval = null
            },
            onDismiss = { pendingApproval = null }
        )
    }
}