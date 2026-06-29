package com.example.fooddelivery.domain.model

import com.example.fooddelivery.data.remote.dto.VoucherDto

data class VoucherPageResult(
    val items: List<VoucherDto>,
    val total: Int,
)
