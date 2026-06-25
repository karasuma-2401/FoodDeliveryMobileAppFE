package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.ConversationDetailDto
import com.example.fooddelivery.data.remote.dto.ConversationDto
import com.example.fooddelivery.data.remote.dto.CreateConversationRequest
import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.MessageDto
import com.example.fooddelivery.data.remote.dto.UploadImageResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {
    @GET("conversation/me")
    suspend fun getConversations(): Response<BaseResponse<List<ConversationDto>>>

    @POST("conversation")
    suspend fun createConversation(
        @Body request: CreateConversationRequest
    ): Response<BaseResponse<ConversationDto>>

    @GET("conversation/detail")
    suspend fun getConversationDetailByOrder(
        @Query("orderId") orderId: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<BaseResponse<ConversationDetailDto>>

    @GET("conversation/{conversationId}")
    suspend fun getConversationDetail(
        @Path("conversationId") conversationId: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<BaseResponse<ConversationDetailDto>>

    @GET("chat/messages/{conversationId}")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<BaseResponse<List<MessageDto>>>

    @Multipart
    @POST("conversation/upload-image")
    suspend fun uploadImage(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part
    ): Response<BaseResponse<UploadImageResponse>>

    @PATCH("conversation/{conversationId}/read")
    suspend fun markAsRead(
        @Path("conversationId") conversationId: Int
    ): Response<BaseResponse<Unit>>
}
