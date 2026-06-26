package com.example.fooddelivery.ui.screens.customer.voucher

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import java.util.Locale

data class VoucherVisual(
    val icon: ImageVector,
    val accentColor: Color,
    val discountLabel: String
)

@Composable
fun Voucher.toVisual(calculatedDiscount: Double = discountAmount): VoucherVisual {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    return when (type) {
        VoucherType.PERCENT -> VoucherVisual(
            icon = Icons.Default.Percent,
            accentColor = primary,
            discountLabel = "${discountAmount.toInt()}% OFF"
        )
        VoucherType.MONEY -> VoucherVisual(
            icon = Icons.Default.Sell,
            accentColor = secondary,
            discountLabel = "-$${String.format(Locale.US, "%.0f", discountAmount)}"
        )
    }.let { visual ->
        when {
            type == VoucherType.MONEY && calculatedDiscount > 0 -> visual.copy(
                discountLabel = "-$${String.format(Locale.US, "%.2f", calculatedDiscount)}"
            )
            type == VoucherType.PERCENT &&
                calculatedDiscount > 0 &&
                calculatedDiscount != discountAmount -> visual.copy(
                discountLabel = "-$${String.format(Locale.US, "%.2f", calculatedDiscount)}"
            )
            else -> visual
        }
    }
}
