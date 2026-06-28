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
                  "addressDetail": "Floor 5",
                  "address": {
                    "id": 10,
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
        assertEquals("Floor 5", address.deliveryNote)
        assertEquals(1, address.id)

        val locationState = DeliveryLocationState(
            addresses = listOf(address),
            selectedAddressId = address.id,
        )

        assertEquals("Home", locationState.selectedAddressLabel)
        assertEquals("123 Nguyen Hue, District 1, HCM", locationState.selectedAddressDetail)
    }

    @Test
    fun `create request maps addressDetail to top level and map bundle to address`() {
        val address = com.example.fooddelivery.domain.model.Address(
            type = "Home",
            title = "Home",
            detail = "123 Nguyen Hue",
            deliveryNote = "Gate B",
            latitude = 10.77,
            longitude = 106.70,
        )

        val request = address.toCreateUserAddressRequest()

        assertEquals("Home", request.title)
        assertEquals("Gate B", request.addressDetail)
        assertEquals(10.77, request.address?.latitude)
        assertEquals(106.70, request.address?.longitude)
        assertEquals("123 Nguyen Hue", request.address?.fullText)
    }

    @Test
    fun `update details request only includes title and addressDetail`() {
        val address = com.example.fooddelivery.domain.model.Address(
            id = 5,
            type = "Work",
            detail = "123 Nguyen Hue",
            deliveryNote = "Tower B, floor 8",
            latitude = 10.77,
            longitude = 106.70,
        )

        val request = address.toUpdateUserAddressRequest()

        assertEquals("Work", request.title)
        assertEquals("Tower B, floor 8", request.addressDetail)
    }

    @Test
    fun `update location request includes photon title and map bundle`() {
        val address = com.example.fooddelivery.domain.model.Address(
            title = "Học viện Chính trị Quốc gia Hồ Chí Minh",
            detail = "135 Nguyễn Thái Học, Ba Đình, Hà Nội",
            latitude = 21.0447918,
            longitude = 105.7883504,
        )

        val request = address.toUpdateUserAddressLocationRequest()

        assertEquals("Học viện Chính trị Quốc gia Hồ Chí Minh", request.title)
        assertEquals("135 Nguyễn Thái Học, Ba Đình, Hà Nội", request.fullText)
        assertEquals(21.0447918, request.latitude, 0.0001)
        assertEquals(105.7883504, request.longitude, 0.0001)
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
