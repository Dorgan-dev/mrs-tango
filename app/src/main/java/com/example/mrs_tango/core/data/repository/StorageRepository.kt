package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class UploadedFileResult(
    val publicUrl: String,
    val storagePath: String
)

class StorageRepository {

    suspend fun uploadImage(
        bucketName: String,
        folderPrefix: String,
        bytes: ByteArray
    ): Result<UploadedFileResult> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "${folderPrefix}_${UUID.randomUUID()}.jpg"
                val bucket = SupabaseClient.client.storage.from(bucketName)

                bucket.upload(path = fileName, data = bytes) {
                    upsert = true
                }

                val publicUrl = bucket.publicUrl(fileName)
                Result.Success(UploadedFileResult(publicUrl = publicUrl, storagePath = fileName))
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal mengunggah gambar")
            }
        }
    }

    suspend fun deleteImage(bucketName: String, storagePath: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.storage.from(bucketName).delete(storagePath)
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal menghapus gambar")
            }
        }
    }
}