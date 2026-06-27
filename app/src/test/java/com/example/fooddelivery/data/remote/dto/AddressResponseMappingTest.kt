package com.example.fooddelivery.data.remote.dto

import com.example.fooddelivery.domain.repository.DeliveryLocationState
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * GET user/address/all is called from:
 * - DeliveryLocationRepository (Home + Search "DELIVER TO")
 * - CheckoutViewModel (checkout address preselect)
 * - CustomerAddressViewModel (address management screen)
 */
class AddressResponseMappingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `API response with coordinates maps to domain address`() {
        val payload = """
            {
              "success": true,
              "data": [
                {
                  "id": 1,
                  "title": "Home",
                  "address": {
                    "title": "Nhà riêng",
                    "latitude": 10.776889,
                    "longitude": 106.700806,
                    "fullText": "123 Nguyen Hue, District 1, HCM"
                  }
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<BaseResponse<List<AddressResponse>>>(payload)
        val address = response.data!!.first().toAddress()

        assertEquals(10.776889, address.latitude, 0.0001)
        assertEquals(106.700806, address.longitude, 0.0001)
        assertEquals("Home", address.type)
        assertEquals("123 Nguyen Hue, District 1, HCM", address.detail)

        val locationState = DeliveryLocationState(
            addresses = listOf(address),
            selectedAddressId = address.id,
        )

        assertEquals("Home", locationState.selectedAddressLabel)
        assertEquals("123 Nguyen Hue, District 1, HCM", locationState.selectedAddressDetail)
    }

    @Test
    fun `empty addresses show placeholder detail`() {
        val locationState = DeliveryLocationState()

        assertEquals(DeliveryLocationState.EMPTY_PLACEHOLDER, locationState.selectedAddressDetail)
        assertTrue(locationState.availableAddressOptions.isEmpty())
    }

    @Test
    fun `API response without coordinates becomes zero and delivery location has no lat lng`() {
        val payload = """
            {
              "success": true,
              "data": [
                {
                  "id": 2,
                  "title": "Work",
                  "address": {
                    "title": "Văn phòng",
                    "fullText": "456 Le Loi, District 1, HCM"
                  }
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<BaseResponse<List<AddressResponse>>>(payload)
        val address = response.data!!.first().toAddress()

        assertEquals(0.0, address.latitude, 0.0001)
        assertEquals(0.0, address.longitude, 0.0001)

        val locationState = DeliveryLocationState(
            addresses = listOf(address),
            selectedAddressId = address.id,
        )

        assertNull(locationState.lat)
        assertNull(locationState.lng)
    }
}
