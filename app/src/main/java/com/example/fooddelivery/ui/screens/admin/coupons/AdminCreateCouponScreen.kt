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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.ui.screens.admin.components.*
import com.example.fooddelivery.ui.screens.admin.dashboard.AdminRestaurantScreen
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCreateCouponScreen(
    onNavigateBack: () -> Unit
) {
    var couponCode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("Percentage Discount") }
    var discountValue by remember { mutableStateOf("0") }
    var maxDiscount by remember { mutableStateOf("0.00") }

    var minOrder by remember { mutableStateOf("25") }
    var perUserLimit by remember { mutableStateOf("1") }
    var totalUsageLimit by remember { mutableStateOf("500") }

    var startDate by remember { mutableStateOf("10/01/2026") }
    var endDate by remember { mutableStateOf("12/31/2026") }
    var neverExpires by remember { mutableStateOf(false) }

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
                        onClick = { /* Xử lý nhanh sự kiện Lưu giống icon tích trên góc phải */ },
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
                    code = couponCode, onCodeChange = { couponCode = it },
                    description = description, onDescriptionChange = { description = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                DiscountDetailsSection(
                    discountType = discountType, onTypeChange = { discountType = it },
                    discountValue = discountValue, onValueChange = { discountValue = it },
                    maxDiscount = maxDiscount, onMaxDiscountChange = { maxDiscount = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                UsageRulesSection(
                    minOrder = minOrder, onMinOrderChange = { minOrder = it },
                    perUserLimit = perUserLimit, onPerUserChange = { perUserLimit = it },
                    totalLimit = totalUsageLimit, onTotalLimitChange = { totalUsageLimit = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SchedulingSection(
                    startDate = startDate, endDate = endDate, neverExpires = neverExpires,
                    onNeverExpiresChange = { neverExpires = it },
                    onStartDateClick = { /* Mở DatePickerDialog */ },
                    onEndDateClick = { /* Mở DatePickerDialog */ }
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
                    onClick = { /* Thực thi Lưu dữ liệu Form lên Server */ },
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
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel & Discard", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminCreateCouponScreenReview() {
    DFoodTheme {
        AdminCreateCouponScreen(
            onNavigateBack = {},
        )
    }
}