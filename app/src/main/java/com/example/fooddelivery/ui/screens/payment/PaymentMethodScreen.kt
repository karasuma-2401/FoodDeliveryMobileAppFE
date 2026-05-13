package com.example.fooddelivery.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.payment.components.CodMethodInfo
import com.example.fooddelivery.ui.screens.payment.components.EmptyMoMoState
import com.example.fooddelivery.ui.screens.payment.components.MoMoPaymentItem
import com.example.fooddelivery.ui.screens.payment.components.SecurityNote
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun PaymentMethodScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentMethodViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PaymentMethodContent(
        state = state,
        onNavigateBack = onNavigateBack
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodContent(
    state: PaymentMethodState,
    onNavigateBack: () -> Unit,
    viewModel: PaymentMethodViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Payment Methods",
                onBackClick = onNavigateBack
            )
        },
        containerColor = Color.White,
        bottomBar = {
            Button(
                onClick = { viewModel.onEvent(PaymentMethodEvent.LinkNewMoMo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA50064)
                ),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "LINK NEW MOMO WALLET",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Linked Wallets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF32343E)
                    )
                    Text(
                        text = "Manage your MoMo accounts for 1-touch payment",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF646982)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (state.linkedMoMoMethods.isEmpty() && !state.isLoading) {
                    item {
                        EmptyMoMoState()
                    }
                } else {
                    items(state.linkedMoMoMethods) { method ->
                        MoMoPaymentItem(
                            method = method,
                            onUnlink = { viewModel.onEvent(PaymentMethodEvent.UnlinkMoMo(method.id)) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Other Methods",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF32343E)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CodMethodInfo()
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SecurityNote()
                }
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PaymentMethodScreenPreview() {
    DFoodTheme(darkTheme = false) {
        PaymentMethodContent(
            state = PaymentMethodState(),
            onNavigateBack = {}
        )
    }
}


