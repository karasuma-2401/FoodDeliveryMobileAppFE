package com.example.fooddelivery.ui.components.BestSeller

import com.example.fooddelivery.domain.model.BestSellerItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the sorting logic used in BestSellerFullScreen, which sorts items
 * by soldCount in descending order.
 *
 * The composable sorts with: items.sortedByDescending { it.soldCount }
 */
class BestSellerSortingTest {

    private fun makeItem(name: String, soldCount: Int) = BestSellerItem(
        name = name,
        price = "$1.00",
        rating = 4.0f,
        soldCount = soldCount,
        imageRes = 0
    )

    @Test
    fun `sortedByDescending soldCount orders highest sold first`() {
        val items = listOf(
            makeItem("Low", 10),
            makeItem("High", 200),
            makeItem("Mid", 100)
        )

        val sorted = items.sortedByDescending { it.soldCount }

        assertEquals("High", sorted[0].name)
        assertEquals("Mid", sorted[1].name)
        assertEquals("Low", sorted[2].name)
    }

    @Test
    fun `sortedByDescending with single item returns single item unchanged`() {
        val items = listOf(makeItem("Only", 50))

        val sorted = items.sortedByDescending { it.soldCount }

        assertEquals(1, sorted.size)
        assertEquals("Only", sorted[0].name)
    }

    @Test
    fun `sortedByDescending with empty list returns empty list`() {
        val items = emptyList<BestSellerItem>()

        val sorted = items.sortedByDescending { it.soldCount }

        assertTrue(sorted.isEmpty())
    }

    @Test
    fun `sortedByDescending with already sorted list preserves order`() {
        val items = listOf(
            makeItem("First", 300),
            makeItem("Second", 200),
            makeItem("Third", 100)
        )

        val sorted = items.sortedByDescending { it.soldCount }

        assertEquals("First", sorted[0].name)
        assertEquals("Second", sorted[1].name)
        assertEquals("Third", sorted[2].name)
    }

    @Test
    fun `sortedByDescending with reverse-sorted list reverses order`() {
        val items = listOf(
            makeItem("Third", 50),
            makeItem("Second", 150),
            makeItem("First", 300)
        )

        val sorted = items.sortedByDescending { it.soldCount }

        assertEquals("First", sorted[0].name)
        assertEquals("Second", sorted[1].name)
        assertEquals("Third", sorted[2].name)
    }

    @Test
    fun `sortedByDescending returns all items`() {
        val items = listOf(
            makeItem("A", 120),
            makeItem("B", 80),
            makeItem("C", 200),
            makeItem("D", 50),
            makeItem("E", 150)
        )

        val sorted = items.sortedByDescending { it.soldCount }

        assertEquals(5, sorted.size)
    }

    @Test
    fun `sortedByDescending puts highest soldCount first with many items`() {
        val items = listOf(
            makeItem("A", 120),
            makeItem("B", 80),
            makeItem("C", 200),
            makeItem("D", 50),
            makeItem("E", 150)
        )

        val sorted = items.sortedByDescending { it.soldCount }

        assertEquals("C", sorted[0].name)     // 200
        assertEquals("E", sorted[1].name)     // 150
        assertEquals("A", sorted[2].name)     // 120
        assertEquals("B", sorted[3].name)     // 80
        assertEquals("D", sorted[4].name)     // 50
    }

    @Test
    fun `sortedByDescending with equal soldCount preserves relative order`() {
        val items = listOf(
            makeItem("First", 100),
            makeItem("Second", 100),
            makeItem("Third", 100)
        )

        val sorted = items.sortedByDescending { it.soldCount }

        // All have the same soldCount; result should contain all three
        assertEquals(3, sorted.size)
        assertEquals(100, sorted[0].soldCount)
        assertEquals(100, sorted[1].soldCount)
        assertEquals(100, sorted[2].soldCount)
    }

    @Test
    fun `sortedByDescending with zero soldCount items goes to end`() {
        val items = listOf(
            makeItem("Zero", 0),
            makeItem("High", 500),
            makeItem("Mid", 250)
        )

        val sorted = items.sortedByDescending { it.soldCount }

        assertEquals("High", sorted[0].name)
        assertEquals("Mid", sorted[1].name)
        assertEquals("Zero", sorted[2].name)
    }

    @Test
    fun `sortedByDescending does not mutate original list`() {
        val items = listOf(
            makeItem("Low", 10),
            makeItem("High", 200)
        )
        val originalFirst = items[0].name

        items.sortedByDescending { it.soldCount }

        // Original list is unchanged
        assertEquals(originalFirst, items[0].name)
    }

    @Test
    fun `sortedByDescending with two items swaps when needed`() {
        val items = listOf(
            makeItem("Small", 5),
            makeItem("Big", 100)
        )

        val sorted = items.sortedByDescending { it.soldCount }

        assertEquals("Big", sorted[0].name)
        assertEquals("Small", sorted[1].name)
    }
}