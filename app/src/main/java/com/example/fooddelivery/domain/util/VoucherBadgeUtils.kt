package com.example.fooddelivery.domain.util

import com.example.fooddelivery.data.remote.dto.VoucherDto
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType

data class DiscountBadgeVisual(
    val topLine: String,
    val bottomLine: String
)

fun List<VoucherDto>.pickBestBadgeLabel(): String? {
    val active = filter { it.status.uppercase() == "APPLYING" }
    val bestPercent = active
        .filter { it.type.uppercase() == "PERCENT" }
        .maxByOrNull { it.sale }
    if (bestPercent != null) {
        return "-${bestPercent.sale.toInt()}%"
    }
    val bestMoney = active
        .filter { it.type.uppercase() == "MONEY" }
        .maxByOrNull { it.sale }
    if (bestMoney != null) {
        return "$${bestMoney.sale.toInt()} OFF"
    }
    return null
}

fun List<Voucher>.pickBestDomainBadgeLabel(): String? {
    val bestPercent = this
        .filter { it.type == VoucherType.PERCENT }
        .maxByOrNull { it.discountAmount }
    if (bestPercent != null) {
        return "${bestPercent.discountAmount.toInt()}% OFF"
    }
    val bestMoney = this
        .filter { it.type == VoucherType.MONEY }
        .maxByOrNull { it.discountAmount }
    if (bestMoney != null) {
        return "$${bestMoney.discountAmount.toInt()} OFF"
    }
    return null
}

fun List<Voucher>.pickBestDiscountBadgeVisual(): DiscountBadgeVisual? {
    val bestPercent = this
        .filter { it.type == VoucherType.PERCENT }
        .maxByOrNull { it.discountAmount }
    if (bestPercent != null) {
        return DiscountBadgeVisual(
            topLine = "${bestPercent.discountAmount.toInt()}%",
            bottomLine = "OFF"
        )
    }
    val bestMoney = this
        .filter { it.type == VoucherType.MONEY }
        .maxByOrNull { it.discountAmount }
    if (bestMoney != null) {
        return DiscountBadgeVisual(
            topLine = "$${bestMoney.discountAmount.toInt()}",
            bottomLine = "OFF"
        )
    }
    return null
}

fun String.toDiscountBadgeVisual(): DiscountBadgeVisual? {
    val percentMatch = Regex("""(\d+)\s*%""").find(trim())
    if (percentMatch != null) {
        return DiscountBadgeVisual(
            topLine = "${percentMatch.groupValues[1]}%",
            bottomLine = "OFF"
        )
    }
    val moneyMatch = Regex("""\$(\d+)""").find(trim())
    if (moneyMatch != null) {
        return DiscountBadgeVisual(
            topLine = "$${moneyMatch.groupValues[1]}",
            bottomLine = "OFF"
        )
    }
    return null
}
