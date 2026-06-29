package com.example.fooddelivery.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageUriCompressor {

    fun compressToJpegFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            inputStream.use { stream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = false
                    inSampleSize = 2
                }
                val bitmap = BitmapFactory.decodeStream(stream, null, options) ?: return null
                val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
                }
                bitmap.recycle()
                file.takeIf { it.exists() && it.length() > 0L }
            }
        } catch (_: Exception) {
            null
        }
    }
}
