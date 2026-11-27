package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.UserModel
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val storageRepository: StorageRepository = StorageRepository()
) {

    // Mengambil data pengguna berdasarkan ID.
    suspend fun getUserById(userId: String): Result<UserModel> {
        return withContext(Dispatchers.IO) {
            try {
                val user = SupabaseClient.client.from("users")
                    .select {
                        filter { eq("id", userId) }
                    }
                    .decodeSingle<UserModel>()
                Result.Success(user)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat profil pengguna")
            }
        }
    }

    // Memperbarui profil pengguna beserta foto avatar jika ada perubahan.
    suspend fun updateUserProfile(
        userId: String,
        name: String,
        phone: String?,
        newAvatarBytes: ByteArray? = null,
        oldAvatarUrl: String? = null
    ): Result<UserModel> {
        return withContext(Dispatchers.IO) {
            try {
                var finalAvatarUrl = oldAvatarUrl

                if (newAvatarBytes != null) {
                    val uploadResult = storageRepository.uploadImage(
                        bucketName = "avatars",
                        folderPrefix = "user_$userId",
                        bytes = newAvatarBytes
                    )

                    if (uploadResult is Result.Error) {
                        return@withContext Result.Error(uploadResult.message)
                    }

                    finalAvatarUrl = (uploadResult as Result.Success).data.publicUrl

                    if (!oldAvatarUrl.isNullOrBlank() && oldAvatarUrl.contains("/avatars/")) {
                        val oldFileName = oldAvatarUrl.substringAfter("/avatars/")
                        if (oldFileName.isNotBlank()) {
                            storageRepository.deleteImage("avatars", oldFileName)
                        }
                    }
                }

                val updatedUser = SupabaseClient.client.from("users")
                    .update(
                        mapOf(
                            "name" to name,
                            "phone" to phone?.ifBlank { null },
                            "avatar" to finalAvatarUrl
                        )
                    ) {
                        filter { eq("id", userId) }
                        select()
                    }
                    .decodeSingle<UserModel>()

                Result.Success(updatedUser)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memperbarui profil")
            }
        }
    }
}