package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.ConversationDto
import com.example.fooddelivery.data.remote.dto.MessageDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {
    @GET("chat/conversations")
    suspend fun getConversations(): Response<List<ConversationDto>>

    @GET("chat/messages/{conversationId}")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<List<MessageDto>>

    @Multipart
    @POST("chat/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<String>
}