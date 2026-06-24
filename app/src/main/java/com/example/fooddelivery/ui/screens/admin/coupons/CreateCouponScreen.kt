package com.example.fooddelivery.ui.screens.admin.coupons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fooddelivery.ui.screens.admin.components.*
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCouponScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateCouponViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSavedSuccessfully) {
        if (uiState.isSavedSuccessfully) {
            onNavigateBack()
            viewModel.clearNavigationFlag()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Coupon", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveCoupon() },
                        enabled = !uiState.isSaving,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(18.dp))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    GeneralInfoSection(
                        code = uiState.couponCode,
                        onCodeChange = { viewModel.onCouponCodeChange(it) },
                        description = uiState.description,
                        onDescriptionChange = { viewModel.onDescriptionChange(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DiscountDetailsSection(
                        discountType = uiState.discountType,
                        onTypeChange = { viewModel.onDiscountTypeChange(it) },
                        discountValue = uiState.discountValue,
                        onValueChange = { viewModel.onDiscountValueChange(it) },
                        maxDiscount = uiState.maxDiscount,
                        onMaxDiscountChange = { viewModel.onMaxDiscountChange(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    UsageRulesSection(
                        minOrder = uiState.minOrder,
                        onMinOrderChange = { viewModel.onMinOrderChange(it) },
                        perUserLimit = uiState.perUserLimit,
                        onPerUserChange = { viewModel.onPerUserLimitChange(it) },
                        totalLimit = uiState.totalUsageLimit,
                        onTotalLimitChange = { viewModel.onTotalUsageLimitChange(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SchedulingSection(
                        startDate = uiState.startDate,
                        endDate = uiState.endDate,
                        neverExpires = uiState.neverExpires,
                        onNeverExpiresChange = { viewModel.onNeverExpiresChange(it) },
                        onStartDateClick = { /* Tích hợp DatePickerDialog nếu cần chỉnh sửa date */ },
                        onEndDateClick = { /* Tích hợp DatePickerDialog nếu cần chỉnh sửa date */ }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveCoupon() },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Coupon", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    }

                    OutlinedButton(
                        onClick = onNavigateBack,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel & Discard", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            if (uiState.isSaving) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateCouponScreenReview() {
    DFoodTheme {
        CreateCouponScreen(
            onNavigateBack = {},
        )
    }
}