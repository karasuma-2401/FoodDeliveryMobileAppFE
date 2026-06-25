package com.example.fooddelivery

import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.FoodRatingResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodRatingResponseTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val apiResponse = """
        {"success":true,"data":{"id":4,"restaurantId":5,"userId":5,"vote":3,"comment":"nothing","reply":null,"replyCreatedAt":null,"createdAt":"2026-06-25T17:37:35.272Z","deleteAt":null,"orderId":5,"tags":["Reasonable price"]}}
    """.trimIndent()

    @Test
    fun `current FoodRatingResponse deserializes API response`() {
        val parsed = json.decodeFromString<BaseResponse<FoodRatingResponse>>(apiResponse)
        assertTrue(parsed.success == true)
        assertNotNull(parsed.data)
    }
}
