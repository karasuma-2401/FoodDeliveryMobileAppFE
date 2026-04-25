package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.PhotonResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotonService {
    @GET("api")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("lang") lang: String = "en"
    ): PhotonResponse

    companion object {
        const val BASE_URL = "https://photon.komoot.io/"
    }
}
