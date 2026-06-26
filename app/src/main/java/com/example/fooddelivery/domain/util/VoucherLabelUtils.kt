package com.example.fooddelivery.domain.util

import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import java.util.Locale

fun Voucher.toTicketLabels(): Pair<String, String> {
    val discountLine = when (type) {
        VoucherType.PERCENT -> "Save ${discountAmount.toInt()}%"
        VoucherType.MONEY -> "Save $${formatUsd(discountAmount)}"
    }
    val minOrderLine = "Min. order $${formatUsd(minOrderAmount)}"
    return discountLine to minOrderLine
}

private fun formatUsd(amount: Double): String =
    if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", amount)
    }
