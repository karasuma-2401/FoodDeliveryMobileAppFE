package com.example.fooddelivery.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class RelativeTimeFormatterTest {

    private val now = 1_700_000_000_000L

    @Test
    fun `shows just now for recent timestamps`() {
        assertEquals("Just now", formatRelativeTimeEnglish(now - 30_000, now))
    }

    @Test
    fun `shows minutes ago`() {
        assertEquals("1 min ago", formatRelativeTimeEnglish(now - TimeUnit.MINUTES.toMillis(1), now))
        assertEquals("5 min ago", formatRelativeTimeEnglish(now - TimeUnit.MINUTES.toMillis(5), now))
    }

    @Test
    fun `shows hours ago`() {
        assertEquals("1 hr ago", formatRelativeTimeEnglish(now - TimeUnit.HOURS.toMillis(1), now))
        assertEquals("3 hr ago", formatRelativeTimeEnglish(now - TimeUnit.HOURS.toMillis(3), now))
    }

    @Test
    fun `shows yesterday`() {
        assertEquals("Yesterday", formatRelativeTimeEnglish(now - TimeUnit.DAYS.toMillis(1), now))
    }

    @Test
    fun `shows days ago`() {
        assertEquals("3 days ago", formatRelativeTimeEnglish(now - TimeUnit.DAYS.toMillis(3), now))
    }

    @Test
    fun `shows absolute date for older timestamps`() {
        val timestamp = 1_698_192_000_000L // Oct 25, 2023 UTC
        assertEquals("Oct 25, 2023", formatRelativeTimeEnglish(timestamp, now))
    }
}
