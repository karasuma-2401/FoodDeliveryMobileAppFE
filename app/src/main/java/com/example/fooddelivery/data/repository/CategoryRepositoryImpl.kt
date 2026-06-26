package com.example.fooddelivery.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.fooddelivery.data.remote.api.CategoryApi
import com.example.fooddelivery.data.remote.dto.CategoryDetailResponse
import com.example.fooddelivery.data.remote.dto.CategoryResponse
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.data.remote.unwrapUnit
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.CategoryDetail
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.repository.CategoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class CategoryRepositoryImpl @Inject constructor(
    private val api: CategoryApi,
    @ApplicationContext private val context: Context
) : CategoryRepository {

    override suspend fun getCategories(
        keyword: String?,
        limit: Int?,
        offset: Int?,
        isActive: Boolean?
    ): Result<List<Category>> {
        return try {
            api.getCategories(keyword, limit, offset, isActive)
                .unwrapData("Failed to load categories")
                .map { baseListResponse ->
                    baseListResponse.data ?: emptyList()
                }
                .map { list -> list.map { it.toDomain() } }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getCategoryById(id: Int): Result<CategoryDetail> {
        return try {
            api.getCategoryById(id)
                .unwrapData("Failed to load category")
                .map { it.toDetailDomain() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    override suspend fun createCategory(
        name: String,
        description: String,
        displayOrder: Int,
        isActive: Boolean,
        imageUri: String?
    ): Result<Category> {
        var tempFile: File? = null
        return try {
            val response = api.createCategory(
                name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                displayOrder = displayOrder.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                isActive = isActive.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                image = createImagePart(imageUri)?.also { tempFile = it.second }?.first
            )
            response.unwrapData("Failed to create category").map { it.toDomain() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        } finally {
            tempFile?.delete()
        }
    }

    override suspend fun updateCategory(
        id: Int,
        name: String?,
        description: String?,
        displayOrder: Int?,
        isActive: Boolean?,
        imageUri: String?
    ): Result<Category> {
        var tempFile: File? = null
        return try {
            val response = api.updateCategory(
                id = id,
                name = name?.toRequestBody("text/plain".toMediaTypeOrNull()),
                description = description?.toRequestBody("text/plain".toMediaTypeOrNull()),
                displayOrder = displayOrder?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                isActive = isActive?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                image = createImagePart(imageUri)?.also { tempFile = it.second }?.first
            )
            response.unwrapData("Failed to update category").map { it.toDomain() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        } finally {
            tempFile?.delete()
        }
    }

    override suspend fun deleteCategory(id: Int): Result<Unit> {
        return try {
            api.deleteCategory(id).unwrapUnit("Failed to delete category")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }


    private fun CategoryResponse.toDomain(): Category {
        return Category(
            id = id.toString(),
            name = name,
            imageUrl = image,
            description = description,
            foodCount = foodCount ?: 0,
            displayOrder = displayOrder ?: sortOrder ?: 0,
            isActive = isActive ?: true
        )
    }

    private fun CategoryDetailResponse.toDetailDomain(): CategoryDetail {
        return CategoryDetail(
            id = id.toString(),
            name = name,
            imageUrl = image,
            description = description,
            foods = foods.orEmpty().map { food ->
                FoodItem(
                    id = food.id.toString(),
                    name = food.name,
                    restaurantId = food.restaurant?.id?.toString() ?: "",
                    restaurantName = food.restaurant?.name ?: "",
                    categoryId = id.toString(),
                    price = food.price,
                    imageUrl = food.image
                )
            }
        )
    }

    private fun createImagePart(imageUri: String?): Pair<MultipartBody.Part, File>? {
        if (imageUri.isNullOrBlank() || !imageUri.startsWith("content://")) return null

        val tempFile = getCompressedFileFromUri(Uri.parse(imageUri)) ?: return null
        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
        return part to tempFile
    }

    private fun getCompressedFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = 2
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options) ?: return null

            val file = File(context.cacheDir, "category_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
            }
            bitmap.recycle()
            file
        } catch (_: Exception) {
            null
        }
    }
}
