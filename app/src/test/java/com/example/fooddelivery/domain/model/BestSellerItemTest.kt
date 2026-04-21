package com.example.fooddelivery.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BestSellerItemTest {

    @Test
    fun `creation with all fields stores correct values`() {
        val item = BestSellerItem(
            name = "Burger",
            price = "$5.99",
            rating = 4.5f,
            soldCount = 120,
            imageRes = 123456
        )

        assertEquals("Burger", item.name)
        assertEquals("$5.99", item.price)
        assertEquals(4.5f, item.rating, 0.001f)
        assertEquals(120, item.soldCount)
        assertEquals(123456, item.imageRes)
    }

    @Test
    fun `two items with same values are equal`() {
        val item1 = BestSellerItem("Pizza", "$8.99", 4.8f, 200, 111)
        val item2 = BestSellerItem("Pizza", "$8.99", 4.8f, 200, 111)

        assertEquals(item1, item2)
    }

    @Test
    fun `two items with different names are not equal`() {
        val item1 = BestSellerItem("Pizza", "$8.99", 4.8f, 200, 111)
        val item2 = BestSellerItem("Burger", "$8.99", 4.8f, 200, 111)

        assertNotEquals(item1, item2)
    }

    @Test
    fun `two items with different soldCount are not equal`() {
        val item1 = BestSellerItem("Pizza", "$8.99", 4.8f, 200, 111)
        val item2 = BestSellerItem("Pizza", "$8.99", 4.8f, 199, 111)

        assertNotEquals(item1, item2)
    }

    @Test
    fun `copy creates new item with overridden field`() {
        val original = BestSellerItem("Chicken", "$6.49", 4.6f, 150, 222)
        val updated = original.copy(soldCount = 300, price = "$7.99")

        assertEquals("Chicken", updated.name)
        assertEquals("$7.99", updated.price)
        assertEquals(4.6f, updated.rating, 0.001f)
        assertEquals(300, updated.soldCount)
        assertEquals(222, updated.imageRes)
    }

    @Test
    fun `copy preserves unmodified fields`() {
        val original = BestSellerItem("Sushi", "$12.00", 5.0f, 500, 333)
        val copy = original.copy(name = "Ramen")

        assertEquals("Ramen", copy.name)
        assertEquals("$12.00", copy.price)
        assertEquals(5.0f, copy.rating, 0.001f)
        assertEquals(500, copy.soldCount)
        assertEquals(333, copy.imageRes)
    }

    @Test
    fun `item with zero soldCount is valid`() {
        val item = BestSellerItem("New Item", "$1.00", 0.0f, 0, 0)

        assertEquals(0, item.soldCount)
        assertEquals(0.0f, item.rating, 0.001f)
    }

    @Test
    fun `item with maximum rating of 5 is valid`() {
        val item = BestSellerItem("Top Item", "$9.99", 5.0f, 9999, 444)

        assertEquals(5.0f, item.rating, 0.001f)
    }

    @Test
    fun `hashCode is consistent for equal items`() {
        val item1 = BestSellerItem("Tacos", "$4.50", 4.2f, 80, 555)
        val item2 = BestSellerItem("Tacos", "$4.50", 4.2f, 80, 555)

        assertEquals(item1.hashCode(), item2.hashCode())
    }

    @Test
    fun `toString contains item fields`() {
        val item = BestSellerItem("Fries", "$2.99", 4.0f, 300, 666)
        val str = item.toString()

        assert(str.contains("Fries"))
        assert(str.contains("2.99"))
        assert(str.contains("300"))
    }
}