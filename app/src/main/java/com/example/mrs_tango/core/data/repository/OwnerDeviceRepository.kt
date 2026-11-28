package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.DeviceImageModel
import com.example.mrs_tango.core.data.model.DeviceLogModel
import com.example.mrs_tango.core.data.model.DeviceModel
import com.example.mrs_tango.core.data.model.DeviceRateModel
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OwnerDeviceRepository(
    private val storageRepository: StorageRepository = StorageRepository()
) {

    // Method ini digunakan untuk mengambil seluruh data perangkat beserta relasi foto dan tarif dari database Supabase.
    suspend fun getAllDevices(): Result<List<DeviceDetailsModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val columns = Columns.raw("*, device_images(*), device_rates(*)")
                val data = SupabaseClient.client.from("devices")
                    .select(columns = columns) {
                        order(column = "created_at", order = Order.ASCENDING)
                    }
                    .decodeList<DeviceDetailsModel>()
                Result.Success(data)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat daftar perangkat")
            }
        }
    }

    // Method ini digunakan untuk menambahkan perangkat baru ke database sekaligus mencatat aktivitas penambahannya ke tabel device_logs.
    suspend fun createDevice(device: DeviceModel, actorId: String?): Result<DeviceModel> {
        return withContext(Dispatchers.IO) {
            try {
                val inserted = SupabaseClient.client.from("devices")
                    .insert(device) { select() }
                    .decodeSingle<DeviceModel>()

                // Catat log awal pembuatan unit
                inserted.id?.let { newId ->
                    insertLog(
                        deviceId = newId,
                        actorId = actorId,
                        fromStatus = "new",
                        toStatus = inserted.status.toString(),
                        note = "Unit baru ditambahkan oleh Owner"
                    )
                }

                Result.Success(inserted)
            } catch (e: Exception) {
                val msg = if (e.message?.contains("23505") == true || e.message?.contains("unique", true) == true) {
                    "Serial Number Konsol atau TV sudah terdaftar pada unit lain"
                } else {
                    e.message ?: "Gagal menambahkan perangkat"
                }
                Result.Error(msg)
            }
        }
    }

    // Method ini digunakan untuk memperbarui informasi perangkat dan mencatat perubahan status ke tabel device_logs jika status berubah.
    suspend fun updateDevice(
        deviceId: String,
        device: DeviceModel,
        oldStatus: String,
        actorId: String?,
        statusNote: String? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.from("devices").update(
                    mapOf(
                        "name" to device.name,
                        "consol_type" to device.consoleType,
                        "console_sn" to device.consoleSn,
                        "tv_name" to device.tvName,
                        "tv_sn" to device.tvSn,
                        "tv_ip_address" to device.tvIpAddress,
                        "tv_mac_address" to device.tvMacAddress,
                        "status" to device.status,
                        "description" to device.description
                    )
                ) {
                    filter { eq("id", deviceId) }
                }

                if (!oldStatus.equals(device.status, ignoreCase = true)) {
                    insertLog(
                        deviceId = deviceId,
                        actorId = actorId,
                        fromStatus = oldStatus,
                        toStatus = device.status.toString(),
                        note = statusNote?.ifBlank { null } ?: "Perubahan status oleh Owner"
                    )
                }

                Result.Success(true)
            } catch (e: Exception) {
                val msg = if (e.message?.contains("23505") == true || e.message?.contains("unique", true) == true) {
                    "Serial Number Konsol atau TV bentrok dengan unit lain"
                } else {
                    e.message ?: "Gagal memperbarui perangkat"
                }
                Result.Error(msg)
            }
        }
    }

    // Method ini digunakan untuk menghapus perangkat dari database sekaligus menghapus seluruh file foto terkait dari Supabase Storage.
    suspend fun deleteDevice(deviceId: String, storagePaths: List<String>?): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Hapus file fisik di storage terlebih dahulu
                storagePaths?.forEach { path ->
                    if (path.isNotBlank()) {
                        storageRepository.deleteImage("device-images", path)
                    }
                }
                SupabaseClient.client.from("devices").delete {
                    filter { eq("id", deviceId) }
                }
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error("Gagal menghapus perangkat (pastikan tidak ada riwayat booking/sesi aktif): ${e.message}")
            }
        }
    }

    // --- GALERI MULTI-FOTO ---

    // Method ini digunakan untuk mengunggah foto perangkat ke Supabase Storage dan menyimpan informasi foto beserta status utama dan urutan tampilnya ke database.
    suspend fun addDeviceImage(
        deviceId: String,
        imageBytes: ByteArray,
        setAsPrimary: Boolean,
        nextSortOrder: Int
    ): Result<DeviceImageModel> {
        return withContext(Dispatchers.IO) {
            try {
                val upload = storageRepository.uploadImage(
                    bucketName = "device-images",
                    folderPrefix = "device_$deviceId",
                    bytes = imageBytes
                )
                if (upload is Result.Error) return@withContext Result.Error(upload.message)
                val uploaded = (upload as Result.Success).data

                if (setAsPrimary) {
                    SupabaseClient.client.from("device_images").update(mapOf("is_primary" to false)) {
                        filter { eq("device_id", deviceId) }
                    }
                }

                val payload = DeviceImageModel(
                    deviceId = deviceId,
                    imageUrl = uploaded.publicUrl,
                    storagePath = uploaded.storagePath,
                    isPrimary = setAsPrimary,
                    sortOrder = nextSortOrder
                )

                val saved = SupabaseClient.client.from("device_images")
                    .insert(payload) { select() }
                    .decodeSingle<DeviceImageModel>()

                Result.Success(saved)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal mengunggah foto perangkat")
            }
        }
    }

    // Method ini digunakan untuk mengubah foto tertentu menjadi foto utama dengan menonaktifkan status foto utama sebelumnya pada perangkat yang sama.
    suspend fun setPrimaryImage(deviceId: String, imageId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.from("device_images").update(mapOf("is_primary" to false)) {
                    filter { eq("device_id", deviceId) }
                }
                SupabaseClient.client.from("device_images").update(mapOf("is_primary" to true)) {
                    filter { eq("id", imageId) }
                }
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal mengubah foto utama")
            }
        }
    }

    // Method ini digunakan untuk menghapus foto perangkat berdasarkan ID foto sekaligus menghapus file fisiknya dari Supabase Storage.
    suspend fun deleteDeviceImage(imageId: String, storagePath: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (storagePath.isNotBlank()) {
                    storageRepository.deleteImage("device-images", storagePath)
                }
                SupabaseClient.client.from("device_images").delete {
                    filter { eq("id", imageId) }
                }
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal menghapus foto")
            }
        }
    }

    // --- SHIFT TARIF ---

    // Method ini digunakan untuk menambahkan shift tarif baru ke tabel device_rates dan mengembalikan data tarif yang berhasil disimpan.
    suspend fun addDeviceRate(rate: DeviceRateModel): Result<DeviceRateModel> {
        return withContext(Dispatchers.IO) {
            try {
                val saved = SupabaseClient.client.from("device_rates")
                    .insert(rate) { select() }
                    .decodeSingle<DeviceRateModel>()
                Result.Success(saved)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal menambah shift tarif")
            }
        }
    }

    // Method ini digunakan untuk mengaktifkan atau menonaktifkan tarif perangkat berdasarkan ID tarif yang dipilih.
    suspend fun toggleRateActive(rateId: String, isActive: Boolean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.from("device_rates").update(mapOf("is_active" to isActive)) {
                    filter { eq("id", rateId) }
                }
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal mengubah status tarif")
            }
        }
    }

    // Method ini digunakan untuk menghapus satu data shift tarif dari tabel device_rates berdasarkan ID tarif.
    suspend fun deleteDeviceRate(rateId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.from("device_rates").delete {
                    filter { eq("id", rateId) }
                }
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal menghapus tarif")
            }
        }
    }

    // --- AUDIT LOG ---

    // Method ini digunakan untuk mengambil maksimal 50 riwayat aktivitas perangkat terbaru beserta informasi perangkat dan pengguna yang melakukan perubahan.
    suspend fun getRecentDeviceLogs(): Result<List<DeviceLogModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val columns = Columns.raw("*, devices(name), users(name)")
                val logs = SupabaseClient.client.from("device_logs")
                    .select(columns = columns) {
                        order(column = "changed_at", order = Order.DESCENDING)
                        limit(50)
                    }
                    .decodeList<DeviceLogModel>()
                Result.Success(logs)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat log perangkat")
            }
        }
    }

    // Method ini digunakan untuk menyimpan catatan perubahan status perangkat ke tabel device_logs sebagai riwayat audit aktivitas pengguna.
    private suspend fun insertLog(
        deviceId: String,
        actorId: String?,
        fromStatus: String,
        toStatus: String,
        note: String?
    ) {
        try {
            val log = DeviceLogModel(
                deviceId = deviceId,
                actorId = actorId,
                fromStatus = fromStatus,
                toStatus = toStatus,
                note = note
            )
            SupabaseClient.client.from("device_logs").insert(log)
        } catch (_: Exception) {
            // Log bersifat audit non-blocking
        }
    }
}