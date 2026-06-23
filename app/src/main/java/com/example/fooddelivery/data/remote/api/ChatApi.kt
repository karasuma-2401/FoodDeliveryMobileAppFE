package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.ConversationDetailDto
import com.example.fooddelivery.data.remote.dto.ConversationDto
import com.example.fooddelivery.data.remote.dto.ConversationListResponse
import com.example.fooddelivery.data.remote.dto.CreateConversationRequest
import com.example.fooddelivery.data.remote.dto.MessageDto
import com.example.fooddelivery.data.remote.dto.UploadImageResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {
    @GET("api/conversation/me")
    suspend fun getConversations(): Response<ConversationListResponse>

    @POST("api/conversation")
    suspend fun createConversation(
        @Body request: CreateConversationRequest
    ): Response<ConversationDto>

    @GET("api/conversation/detail")
    suspend fun getConversationDetailByOrder(
        @Query("orderId") orderId: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<ConversationDetailDto>

    @GET("api/conversation/{conversationId}")
    suspend fun getConversationDetail(
        @Path("conversationId") conversationId: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<ConversationDetailDto>

    @GET("chat/messages/{conversationId}")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<List<MessageDto>>

    @Multipart
    @POST("api/conversation/upload-image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<UploadImageResponse>

    @PATCH("api/conversation/{conversationId}/read")
    suspend fun markAsRead(
        @Path("conversationId") conversationId: Int
    ): Response<Unit>
}
