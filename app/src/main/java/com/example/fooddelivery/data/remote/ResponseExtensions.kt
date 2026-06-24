package com.example.fooddelivery.data.remote

import com.example.fooddelivery.data.remote.dto.BaseListResponse
import com.example.fooddelivery.data.remote.dto.BaseResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.Response

@Serializable
private data class ApiErrorBody(
    val message: String? = null,
    val success: Boolean? = null
)

private val errorJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

fun Response<*>.parseErrorMessage(fallback: String): String {
    val rawBody = errorBody()?.string()?.takeIf { it.isNotBlank() } ?: return fallback
    return runCatching {
        errorJson.decodeFromString<ApiErrorBody>(rawBody).message
    }.getOrNull()?.takeIf { it.isNotBlank() } ?: rawBody
}

fun <T> Response<BaseResponse<T>>.unwrapData(fallback: String): Result<T> {
    val body = body()
    return if (isSuccessful && body?.data != null && body.success != false) {
        Result.success(body.data)
    } else {
        Result.failure(Exception(parseErrorMessage(body?.message ?: fallback)))
    }
}

fun Response<BaseResponse<Unit>>.unwrapUnit(fallback: String): Result<Unit> {
    val body = body()
    return if (isSuccessful && body?.success != false) {
        Result.success(Unit)
    } else {
        Result.failure(Exception(parseErrorMessage(body?.message ?: fallback)))
    }
}

fun <T> Response<BaseListResponse<T>>.unwrapList(fallback: String): Result<List<T>> {
    val body = body()
    return if (isSuccessful && body?.data != null && body.success != false) {
        Result.success(body.data)
    } else {
        Result.failure(Exception(parseErrorMessage(body?.message ?: fallback)))
    }
}
