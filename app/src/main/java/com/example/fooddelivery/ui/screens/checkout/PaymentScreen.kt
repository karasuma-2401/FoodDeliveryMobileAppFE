package com.example.fooddelivery.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.screens.checkout.components.PaymentOptionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onNavigateBack: () -> Unit,
    onConfirmPayment: (PaymentMethod) -> Unit,
    currentMethod: PaymentMethod,
    totalAmount: Double
) {
    var selectedMethod by remember { mutableStateOf(currentMethod) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Payment Method",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF32343E)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color(0xFF32343E)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    DFoodButton(
                        text = "Confirm",
                        onClick = { onConfirmPayment(selectedMethod) },
                        containerColor = Color(0xFFFF7622)
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cash Option
            PaymentOptionItem(
                title = "Cash",
                subtitle = "Pay on delivery",
                icon = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFF7622).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = Color(0xFFFF7622),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                isSelected = selectedMethod == PaymentMethod.Cash,
                onSelect = { selectedMethod = PaymentMethod.Cash }
            )
            PaymentOptionItem(
                title = "MoMo E-Wallet",
                subtitle = "Pay with MoMo app",
                icon = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFA50064), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "MoMo", 
                            color = Color.White, 
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                },
                isSelected = selectedMethod == PaymentMethod.MoMo,
                onSelect = { selectedMethod = PaymentMethod.MoMo }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
