package com.example.fooddelivery.data.remote

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
