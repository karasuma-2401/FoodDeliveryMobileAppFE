package com.example.fooddelivery.ui.screens.admin.coupons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponSearchBarAndFilters
import com.example.fooddelivery.ui.screens.restaurant.component.coupon.CouponPaginationBar
import com.example.fooddelivery.ui.screens.admin.components.AdminCouponItemCard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCouponScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditCoupon: (String) -> Unit,
    onNavigateToCreateCoupon: () -> Unit,
    viewModel: AdminCouponViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("System Coupons", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCreateCoupon) {
                        Icon(Icons.Default.Add, contentDescription = "Add System Coupon")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            CouponSearchBarAndFilters(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.systemVouchers) { voucher ->
                        AdminCouponItemCard(
                            voucher = voucher,
                            onEditClick = { onNavigateToEditCoupon(voucher.id) },
                            onToggleActive = { isActive ->
                                viewModel.toggleSystemCoupon(voucher.id, isActive)
                            }
                        )
                    }
                }
            }

            CouponPaginationBar(
                startItem = if (uiState.systemVouchers.isEmpty()) 0 else 1,
                endItem = uiState.systemVouchers.size,
                totalItems = uiState.totalItems,
                currentPage = uiState.currentPage,
                onPageClick = { viewModel.onPageChanged(it) }
            )
        }
    }
}