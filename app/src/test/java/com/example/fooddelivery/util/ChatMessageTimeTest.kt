package com.example.fooddelivery.util

import com.example.fooddelivery.data.local.room.entity.MessageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatMessageTimeTest {

    private fun message(
        id: String,
        createdAt: String,
        senderId: String = "1",
        content: String = "hi"
    ) = MessageEntity(
        id = id,
        conversationId = "1",
        senderId = senderId,
        content = content,
        imageUrl = null,
        createdAt = createdAt,
        isSending = false,
        isFailed = false
    )

    @Test
    fun `ISO timestamp with milliseconds is parsed correctly`() {
        val millis = messageCreatedAtMillis("2024-06-27T12:00:00.450Z")
        val baseMillis = messageCreatedAtMillis("2024-06-27T12:00:00.000Z")
        assertTrue(millis > baseMillis)
        assertEquals(450L, millis - baseMillis)
    }

    @Test
    fun `millisecond epoch string is parsed as-is`() {
        assertEquals(1_735_000_000_123L, messageCreatedAtMillis("1735000000123"))
    }

    @Test
    fun `same second messages are ordered by server id`() {
        val sameSecond = messageCreatedAtMillis("2024-06-27T12:00:00.000Z").toString()
        val customer = message(id = "10", createdAt = sameSecond, senderId = "2", content = "đồ ăn ngon")
        val restaurant = message(id = "11", createdAt = sameSecond, senderId = "3", content = "cảm ơn bạn")

        val chronological = listOf(restaurant, customer).sortedWith(messageTimelineComparator())
        assertEquals(listOf("10", "11"), chronological.map { it.id })
    }

    @Test
    fun `messenger display puts newest message first for reverse layout`() {
        val sameSecond = "1735000000000"
        val first = message(id = "9", createdAt = sameSecond)
        val second = message(id = "10", createdAt = sameSecond)

        val display = messagesForMessengerDisplay(listOf(second, first))
        assertEquals(listOf("10", "9"), display.map { it.id })
    }

    @Test
    fun `messages with different milliseconds keep chronological order`() {
        val customer = message(
            id = "10",
            createdAt = messageCreatedAtMillis("2024-06-27T12:00:00.200Z").toString(),
            content = "đồ ăn ngon"
        )
        val restaurant = message(
            id = "11",
            createdAt = messageCreatedAtMillis("2024-06-27T12:00:00.800Z").toString(),
            content = "cảm ơn bạn"
        )

        val chronological = listOf(restaurant, customer).sortedWith(messageTimelineComparator())
        assertEquals(listOf("10", "11"), chronological.map { it.id })
    }
}
