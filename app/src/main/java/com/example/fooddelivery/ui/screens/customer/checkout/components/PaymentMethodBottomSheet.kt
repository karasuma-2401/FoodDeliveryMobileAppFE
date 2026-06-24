package com.example.fooddelivery.ui.screens.customer.checkout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.screens.customer.checkout.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodBottomSheet(
    onDismissRequest: () -> Unit,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    selectedPaymentMethod: PaymentMethod,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.payment_method),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            PaymentOptionItem(
                title = stringResource(R.string.cash),
                subtitle = stringResource(R.string.cash_on_delivery),
                icon = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFF7622).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Payments,
                            null,
                            tint = Color(0xFFFF7622),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                isSelected = selectedPaymentMethod is PaymentMethod.Cash,
                onSelect = { onPaymentMethodSelected(PaymentMethod.Cash) }
            )
            PaymentOptionItem(
                title = stringResource(R.string.momo_ewallet),
                subtitle = stringResource(R.string.pay_with_momo_ewallet),
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
                            fontSize = 10.sp
                        )
                    }
                },
                isSelected = selectedPaymentMethod is PaymentMethod.MoMo,
                onSelect = { onPaymentMethodSelected(PaymentMethod.MoMo) }
            )
        }
    }
}
