package com.example.mrs_tango.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object ImageCompressor {

    fun compressUri(context: Context, uri: Uri, maxDimension: Int = 1024, quality: Int = 75): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val ratio = minOf(
                maxDimension.toFloat() / original.width,
                maxDimension.toFloat() / original.height,
                1.0f
            )
            val targetWidth = (original.width * ratio).toInt()
            val targetHeight = (original.height * ratio).toInt()

            val resized = Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true)
            val outputStream = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            if (resized != original) resized.recycle()
            original.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}