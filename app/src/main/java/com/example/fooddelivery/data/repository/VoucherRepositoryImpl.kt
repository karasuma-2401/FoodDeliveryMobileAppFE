package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.VoucherApi
import com.example.fooddelivery.data.remote.dto.VoucherDto
import com.example.fooddelivery.domain.repository.VoucherRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class VoucherRepositoryImpl @Inject constructor(
    private val api: VoucherApi
) : VoucherRepository {

    override suspend fun getVouchers(
        limit: Int?,
        offset: Int?,
        restaurantId: Int?,
        code: String?,
        status: String?
    ): Result<List<VoucherDto>> {
        return try {
            val response = api.getVouchers(limit, offset, restaurantId, code, status)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(e.localizedMessage ?: "Network error"))
        }
    }

    override suspend fun createVoucher(
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
        endAt: String?
    ): Result<VoucherDto> {
        return try {
            fun text(value: String) = value.toRequestBody("text/plain".toMediaTypeOrNull())
            fun number(value: Double) = value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            fun numberOpt(value: Double?) = value?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            fun intOpt(value: Int?) = value?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            fun textOpt(value: String?) = value?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.createVoucher(
                name = text(name),
                code = text(code),
                description = textOpt(description),
                sale = number(sale),
                type = text(type),
                status = textOpt(status),
                restaurantId = intOpt(restaurantId),
                minimumOrderAmount = numberOpt(minimumOrderAmount),
                maximumDiscountAmount = numberOpt(maximumDiscountAmount),
                startAt = textOpt(startAt),
                endAt = textOpt(endAt),
                image = null
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(e.localizedMessage ?: "Network error"))
        }
    }

    override suspend fun updateVoucherStatus(id: Int, status: String): Result<VoucherDto> {
        return try {
            val statusBody = status.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = api.updateVoucher(id = id, status = statusBody)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(e.localizedMessage ?: "Network error"))
        }
    }
}

