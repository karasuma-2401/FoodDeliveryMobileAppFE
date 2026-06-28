package com.example.fooddelivery.util

import com.example.fooddelivery.domain.model.Address
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressFormatTest {

    @Test
    fun `hasValidCoordinates returns true for non-zero lat lng`() {
        val address = Address(latitude = 10.77, longitude = 106.70)
        assertTrue(address.hasValidCoordinates())
    }

    @Test
    fun `hasValidCoordinates returns false for zero coordinates`() {
        val address = Address(latitude = 0.0, longitude = 0.0)
        assertFalse(address.hasValidCoordinates())
    }

    @Test
    fun `formatLocationLine prefers detail when street and city are blank`() {
        val address = Address(detail = "123 Nguyen Hue, District 1")
        assertTrue(address.formatLocationLine().contains("123 Nguyen Hue"))
    }
}
