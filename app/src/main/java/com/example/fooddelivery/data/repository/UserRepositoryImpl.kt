package com.example.fooddelivery.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.local.room.AppDatabase
import com.example.fooddelivery.data.remote.api.UserApi
import com.example.fooddelivery.data.remote.dto.UserProfileData
import com.example.fooddelivery.data.remote.dto.UserProfileResponse
import com.example.fooddelivery.data.remote.parseErrorMessage
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.data.remote.unwrapList
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.model.UserReview
import com.example.fooddelivery.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


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
                response.body()!!.toUser()
            } else {
                Result.failure(Exception(response.parseErrorMessage("Failed to load profile")))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun updateUserProfile(user: User, imageUri: String?): Result<User> {
        var tempFile: File? = null
        return try {
            var imagePart: MultipartBody.Part? = null
            if (imageUri != null && imageUri.startsWith("content://")) {
                tempFile = getCompressedFileFromUri(context, Uri.parse(imageUri))
                if (tempFile != null) {
                    val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("avatar", tempFile.name, requestFile)
                }
            }

            val response = api.updateUserProfile(
                name = user.fullName.toRequestBody("text/plain".toMediaTypeOrNull()),
                avatar = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toUser()
            } else {
                Result.failure(Exception(response.parseErrorMessage("Failed to update profile")))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        } finally {
            tempFile?.delete()
        }
    }

    private fun UserProfileResponse.toUser(): Result<User> {
        val dto = getFinalData()
        return if (dto != null) {
            Result.success(dto.toUser())
        } else {
            Result.failure(Exception("Invalid profile response from server"))
        }
    }

    override suspend fun addUserPhone(phone: String): Result<User> {
        return try {
            val response = api.updateUserProfile(
                phone = phone.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toUser()
            } else {
                Result.failure(Exception(response.parseErrorMessage("Failed to add phone number")))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    private fun UserProfileData.toUser(): User {
        return User(
            id = id?.toString().orEmpty(),
            fullName = name,
            email = email,
            phone = phone.orEmpty(),
            birthday = birthday ?: "",
            profileImage = avatar
        )
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
            api.getUserReviews(limit, offset)
                .unwrapData("Failed to load reviews")
                .mapCatching { nestedResponse ->
                    nestedResponse.data
                        ?: throw Exception(nestedResponse.message ?: "Failed to load reviews")
                }
                .map { reviews ->
                    reviews.map { dto ->
                        UserReview(
                            id = dto.id.toString(),
                            restaurantId = dto.restaurantId.toString(),
                            restaurantName = dto.restaurantName,
                            restaurantImage = dto.restaurantImage ?: "",
                            rating = dto.vote,
                            comment = dto.comment ?: "",
                            tags = dto.tags ?: emptyList(),
                            reply = dto.reply,
                            createdAt = parseIsoDateTimeToMillis(dto.createdAt),
                            orderId = dto.orderId.toString()
                        )
                    }
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
    private fun parseIsoDateTimeToMillis(isoString: String): Long {
        return try {
            val cleaned = isoString.replace("Z", "+0000")
            val pattern = if (cleaned.contains(".")) "yyyy-MM-dd'T'HH:mm:ss.SSSZ" else "yyyy-MM-dd'T'HH:mm:ssZ"
            val formatter = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            formatter.parse(cleaned)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    override suspend fun getFavoriteRestaurants(limit: Int, offset: Int): Result<List<Restaurant>> {
        return try {
            val response = api.getFavoriteRestaurants(limit, offset)
            val result = response.unwrapList("Failed to load favorite restaurants")
            if (result.isFailure) {
                val errorMsg = when (response.code()) {
                    401 -> "Unauthorized: Please login again"
                    403 -> "Forbidden: You don't have permission"
                    else -> response.parseErrorMessage("Failed to load favorite restaurants")
                }
                return Result.failure(Exception(errorMsg, result.exceptionOrNull()))
            }
            result.map { favoriteRestaurants ->
                favoriteRestaurants.map { dto ->
                    Restaurant(
                        id = dto.id.toString(),
                        name = dto.name,
                        description = dto.description ?: "",
                        tags = dto.tags ?: dto.categories?.map { it.name } ?: emptyList(),
                        rating = (dto.rating ?: dto.averageRating ?: 0.0).toFloat(),
                        deliveryFee = dto.deliveryFee ?: 0.0,
                        imageUrl = dto.image,
                        promoTags = emptyList(),
                        isLiked = dto.isLiked ?: true,
                        totalLikes = dto.totalLikes ?: 0
                    )
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }
}
