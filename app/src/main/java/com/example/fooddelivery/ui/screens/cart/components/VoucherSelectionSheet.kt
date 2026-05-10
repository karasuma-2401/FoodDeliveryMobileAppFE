package com.example.fooddelivery.ui.screens.cart.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherSelectionSheet(
    vouchers: List<Voucher>,
    selectedVoucherId: String?,
    promoCode: String,
    promoError: String?,
    onPromoCodeChange: (String) -> Unit,
    onApplyPromoCode: () -> Unit,
    onVoucherSelected: (Voucher) -> Unit,
    onConfirm: (Voucher?) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedId by remember { mutableStateOf(selectedVoucherId) }
    var tempSelectedVoucher by remember { mutableStateOf<Voucher?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select vouchers",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(36.dp)
                    .background(Color(0xFFF3F3F3), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(16.dp))
                        .padding(start = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = promoCode,
                        onValueChange = onPromoCodeChange,
                        placeholder = { Text("Enter the discounts code", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    Button(
                        onClick = onApplyPromoCode,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxHeight(0.8f).width(90.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7622))
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                if (promoError != null) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(promoError, color = Color.Red, fontSize = 12.sp)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Available Discounts",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(vouchers) { voucher ->
                val isSelected = tempSelectedId == voucher.id
                VoucherItemRow(
                    voucher = voucher,
                    isSelected = isSelected,
                    onSelect = {
                        if (voucher.isApplicable) {
                            tempSelectedId = voucher.id
                            tempSelectedVoucher = voucher
                            onVoucherSelected(voucher)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 16.dp,
            color = Color.White
        ) {
            Button(
                onClick = { onConfirm(tempSelectedVoucher) },
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = tempSelectedId != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7622),
                    disabledContainerColor = Color(0xFFFF7622).copy(alpha = 0.5f)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Using Discount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun VoucherItemRow(
    voucher: Voucher,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val alpha = if (voucher.isApplicable) 1f else 0.5f
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isSelected) Color(0xFFFF7622) else Color(0xFFF0F0F0)),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Part
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(if (voucher.type == VoucherType.DISCOUNT) Color(0xFFFF7622) else Color(0xFFF3F3F3)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (voucher.type == VoucherType.DISCOUNT) Icons.Default.Percent else Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = if (voucher.type == VoucherType.DISCOUNT) Color.White else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = voucher.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = voucher.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
                
                if (voucher.expiryText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFF1E9), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(voucher.expiryText, color = Color(0xFFFF7622), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (voucher.conditionMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFFF7622), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(voucher.conditionMessage, color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF7622))
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
