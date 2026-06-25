package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.MessageResponse
import com.example.fooddelivery.data.remote.dto.NotificationDto
import com.example.fooddelivery.data.remote.dto.UnreadCountData
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @GET("notification/me")
    suspend fun getNotifications(
        @Query("type") type: String? = null,
        @Query("read") read: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): BaseResponse<List<NotificationDto>>

    @GET("notification/me/unread-count")
    suspend fun getUnreadCount(): BaseResponse<UnreadCountData>

    @PATCH("notification/{notificationId}/read")
    suspend fun markAsRead(@Path("notificationId") notificationId: Int): BaseResponse<NotificationDto>

    @PATCH("notification/read-all")
    suspend fun markAllRead(): MessageResponse

    @DELETE("notification/{notificationId}")
    suspend fun deleteNotification(@Path("notificationId") notificationId: Int): MessageResponse
}
