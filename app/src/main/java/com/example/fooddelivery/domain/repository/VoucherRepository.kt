package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.VoucherDto

interface VoucherRepository {
    suspend fun getVouchers(
        limit: Int? = 20,
        offset: Int? = 0,
        restaurantId: Int? = null,
        code: String? = null,
        status: String? = null
    ): Result<List<VoucherDto>>

    suspend fun createVoucher(
        name: String,
        code: String,
        description: String?,
        sale: Double,
        type: String,
        status: String?,
        restaurantId: Int?,
        minimumOrderAmount: Double?,
        maximumDiscountAmount: Double?,
        startAt: String?,
        endAt: String?,
        usageLimit: Int? = null,
        userLimit: Int? = null
    ): Result<VoucherDto>

    suspend fun updateVoucherStatus(
        id: Int,
        status: String
    ): Result<VoucherDto>
}
