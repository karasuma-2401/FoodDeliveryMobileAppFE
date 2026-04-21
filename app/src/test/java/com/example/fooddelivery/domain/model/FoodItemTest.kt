package com.example.fooddelivery.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FoodItemTest {

    @Test
    fun `creation with all fields stores correct values`() {
        val item = FoodItem(
            name = "Margherita Pizza",
            category = "Italian",
            price = "$10.99",
            rating = 4.7f,
            reviewCount = 350,
            imageRes = 789012
        )

        assertEquals("Margherita Pizza", item.name)
        assertEquals("Italian", item.category)
        assertEquals("$10.99", item.price)
        assertEquals(4.7f, item.rating, 0.001f)
        assertEquals(350, item.reviewCount)
        assertEquals(789012, item.imageRes)
    }

    @Test
    fun `two items with same values are equal`() {
        val item1 = FoodItem("Pasta", "Italian", "$8.00", 4.5f, 200, 111)
        val item2 = FoodItem("Pasta", "Italian", "$8.00", 4.5f, 200, 111)

        assertEquals(item1, item2)
    }

    @Test
    fun `two items with different categories are not equal`() {
        val item1 = FoodItem("Noodles", "Chinese", "$7.00", 4.3f, 100, 222)
        val item2 = FoodItem("Noodles", "Japanese", "$7.00", 4.3f, 100, 222)

        assertNotEquals(item1, item2)
    }

    @Test
    fun `two items with different reviewCounts are not equal`() {
        val item1 = FoodItem("Steak", "American", "$20.00", 4.9f, 500, 333)
        val item2 = FoodItem("Steak", "American", "$20.00", 4.9f, 499, 333)

        assertNotEquals(item1, item2)
    }

    @Test
    fun `copy creates item with overridden fields`() {
        val original = FoodItem("Salad", "Healthy", "$5.00", 4.0f, 50, 444)
        val updated = original.copy(price = "$6.50", reviewCount = 75)

        assertEquals("Salad", updated.name)
        assertEquals("Healthy", updated.category)
        assertEquals("$6.50", updated.price)
        assertEquals(4.0f, updated.rating, 0.001f)
        assertEquals(75, updated.reviewCount)
        assertEquals(444, updated.imageRes)
    }

    @Test
    fun `copy preserves unmodified fields`() {
        val original = FoodItem("Soup", "Asian", "$4.50", 4.2f, 120, 555)
        val copy = original.copy(rating = 4.8f)

        assertEquals("Soup", copy.name)
        assertEquals("Asian", copy.category)
        assertEquals("$4.50", copy.price)
        assertEquals(4.8f, copy.rating, 0.001f)
        assertEquals(120, copy.reviewCount)
        assertEquals(555, copy.imageRes)
    }

    @Test
    fun `item with zero reviewCount is valid`() {
        val item = FoodItem("New Dish", "Fusion", "$15.00", 0.0f, 0, 0)

        assertEquals(0, item.reviewCount)
        assertEquals(0.0f, item.rating, 0.001f)
    }

    @Test
    fun `hashCode is consistent for equal items`() {
        val item1 = FoodItem("Sandwich", "Fast Food", "$3.99", 3.8f, 60, 666)
        val item2 = FoodItem("Sandwich", "Fast Food", "$3.99", 3.8f, 60, 666)

        assertEquals(item1.hashCode(), item2.hashCode())
    }

    @Test
    fun `toString contains relevant fields`() {
        val item = FoodItem("Tacos", "Mexican", "$4.99", 4.5f, 180, 777)
        val str = item.toString()

        assert(str.contains("Tacos"))
        assert(str.contains("Mexican"))
        assert(str.contains("4.99"))
    }

    @Test
    fun `items with same name but different categories are not equal`() {
        val item1 = FoodItem("Rice", "Chinese", "$3.00", 4.0f, 100, 888)
        val item2 = FoodItem("Rice", "Japanese", "$3.00", 4.0f, 100, 888)

        assertNotEquals(item1, item2)
    }
}