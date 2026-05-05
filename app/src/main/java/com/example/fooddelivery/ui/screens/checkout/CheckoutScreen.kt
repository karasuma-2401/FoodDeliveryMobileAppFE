package com.example.fooddelivery.ui.screens.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.checkout.components.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTrackOrder: (String) -> Unit,
    onNavigateToAddAddress: () -> Unit,
    onNavigateToPaymentMethod: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is CheckoutUiEffect.NavigateToTrackOrder -> onNavigateToTrackOrder(effect.orderId)
                CheckoutUiEffect.NavigateToAddAddress -> onNavigateToAddAddress()
                CheckoutUiEffect.NavigateToPaymentMethod -> onNavigateToPaymentMethod()
            }
        }
    }

    CheckoutContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutContent(
    state: CheckoutState,
    onEvent: (CheckoutEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Checkout",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(24.dp)
                ) {
                    // Cập nhật logic đổi text nút bấm theo yêu cầu
                    val buttonText = if (state.paymentMethod is PaymentMethod.MoMo) {
                        "Pay with MoMo - $${String.format("%.2f", state.total)}"
                    } else {
                        "Place Order"
                    }

                    DFoodButton(
                        text = buttonText,
                        onClick = { onEvent(CheckoutEvent.PlaceOrder) },
                        isLoading = state.isLoading,
                        containerColor = if (state.paymentMethod is PaymentMethod.MoMo) Color(0xFFA50064) else Color(0xFFFF7622),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                SectionTitle("Delivery Address")
                AddressCard(
                    address = state.address,
                    onChangeClick = { onEvent(CheckoutEvent.ChangeAddress) }
                )

                SectionTitle("Delivery Options")
                DeliveryOptionsCard(
                    selectedOption = state.selectedDeliveryOption,
                    onOptionSelected = { onEvent(CheckoutEvent.DeliveryOptionSelected(it)) }
                )

                SectionTitle("Order Notes")
                OrderNotesCard(
                    note = state.orderNote,
                    onNoteChange = { onEvent(CheckoutEvent.NoteChanged(it)) }
                )

                SectionTitle("Payment Method")
                PaymentMethodCard(
                    paymentMethod = state.paymentMethod,
                    onClick = { onEvent(CheckoutEvent.ChangePaymentMethod) }
                )

                CheckoutBillBreakdown(
                    subtotal = state.subtotal,
                    deliveryFee = state.deliveryFee,
                    discount = state.discount,
                    total = state.total
                )

                Spacer(modifier = Modifier.height(120.dp))
            }
            if (state.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
