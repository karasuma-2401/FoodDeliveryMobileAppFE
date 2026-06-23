package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Backend trả về dạng object thay vì raw array:
 * {
 *   "success": true,
 *   "data": [ ... ]
 * }
 */
@Serializable
data class BaseListResponse<T>(
    val success: Boolean? = null,
    val data: List<T>? = null,
    val message: String? = null
)

