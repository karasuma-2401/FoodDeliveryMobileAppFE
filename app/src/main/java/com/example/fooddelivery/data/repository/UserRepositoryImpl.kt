package com.example.fooddelivery.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.fooddelivery.data.remote.api.UserApi
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.model.UserReview
import com.example.fooddelivery.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.local.room.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.ZonedDateTime

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val tokenManager: TokenManager,
    private val database: AppDatabase,
    @ApplicationContext private val context: Context
) : UserRepository {
    override suspend fun getUserProfile(): Result<User> {
        return try {
            val response = api.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Result.success(
                    User(
                        fullName = dto.name,
                        email = dto.email,
                        phone = dto.phone,
                        birthday = dto.birthday ?: "",
                        profileImage = dto.avatar
                    )
                )
            }
            else {
                Result.failure(Exception("Failed to load profile: ${response.message()}"))
            }
        } catch (e : Exception){
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun updateUserProfile(user: User, imageUri: String?): Result<User> {
        var tempFile: File? = null
        return try {
            val namePart = user.fullName.toRequestBody("text/plain".toMediaTypeOrNull())
            val phonePart = user.phone.toRequestBody("text/plain".toRequestBody("text/plain".toMediaTypeOrNull()).contentType())
            
            var imagePart: MultipartBody.Part? = null
            if (imageUri != null && imageUri.startsWith("content://")) {
                tempFile = getCompressedFileFromUri(context, Uri.parse(imageUri))
                if (tempFile != null) {
                    val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("avatar", tempFile.name, requestFile)
                }
            }

            val response = api.updateUserProfile(
                name = namePart,
                phone = user.phone.toRequestBody("text/plain".toMediaTypeOrNull()),
                avatar = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Result.success(
                    User(
                        fullName = dto.name,
                        email = dto.email,
                        phone = dto.phone,
                        birthday = dto.birthday ?: "",
                        profileImage = dto.avatar
                    )
                )
            } else {
                Result.failure(Exception("Failed to update profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        } finally {
            tempFile?.delete()
        }
    }

    private fun getCompressedFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = 2
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options) ?: return null
            
            val file = File(context.cacheDir, "compressed_avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            bitmap.recycle()

            file
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            try {
                api.logout()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
            tokenManager.clearAuthData()
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getUserReviews(limit: Int, offset: Int): Result<List<UserReview>> {
        return try {
            val response = api.getUserReviews(limit, offset)
            if (response.isSuccessful && response.body() != null) {
                val reviews = response.body()!!.map { dto ->
                    UserReview(
                        id = dto.id.toString(),
                        restaurantId = dto.restaurantId.toString(),
                        restaurantName = dto.restaurantName,
                        restaurantImage = dto.restaurantImage ?: "",
                        rating = dto.vote,
                        comment = dto.comment ?: "",
                        tags = dto.tags,
                        createdAt = try {
                            ZonedDateTime.parse(dto.createdAt).toInstant().toEpochMilli()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        },
                        orderId = dto.orderId.toString()
                    )
                }
                Result.success(reviews)
            } else {
                Result.failure(Exception("Failed to load reviews: ${response.message()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getFavoriteRestaurants(limit: Int, offset: Int): Result<List<Restaurant>> {
        return try {
            val response = api.getFavoriteRestaurants(limit, offset)
            if (response.isSuccessful && response.body() != null) {
                val restaurants = response.body()!!.data.map { dto ->
                    Restaurant(
                        id = dto.id.toString(),
                        name = dto.name,
                        description = dto.description ?: "",
                        tags = dto.tags ?: dto.categories?.map { it.name } ?: emptyList(),
                        rating = (dto.rating ?: dto.averageRating ?: 0.0).toFloat(),
                        deliveryFee = dto.deliveryFee ?: 0.0,
                        imageUrl = dto.image,
                        promoTags = if (dto.deliveryFee == 0.0) listOf("Free Delivery") else emptyList(),
                        isLiked = dto.isLiked ?: true,
                        totalLikes = dto.totalLikes ?: 0
                    )
                }
                Result.success(restaurants)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Unauthorized: Please login again"
                    403 -> "Forbidden: You don't have permission"
                    else -> "Failed to load favorite restaurants: ${response.message()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }
}
