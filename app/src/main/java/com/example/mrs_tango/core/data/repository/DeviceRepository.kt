package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.core.data.model.DeviceImageModel
import com.example.mrs_tango.core.data.model.DeviceModel
import com.example.mrs_tango.core.data.model.DeviceRateModel
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceRepository(
    private val storageRepository: StorageRepository = StorageRepository()
) {

    private val devicesTable = "devices"
    private val deviceImagesTable = "device_images"
    private val deviceRatesTable = "device_rates"
    private val deviceImagesBucket = "devices-images"

    // CRUD data perangkat pada tabel devices

    // Method ini digunakan untuk mengambil seluruh data dasar perangkat dari tabel devices tanpa menyertakan relasi foto dan tarif.
    suspend fun getAllDevices(): Result<List<DeviceModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val devices = SupabaseClient.client
                    .from(devicesTable)
                    .select {
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<DeviceModel>()

                Result.Success(devices)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat data perangkat")
            }
        }
    }

    // Method ini digunakan untuk mengambil seluruh data perangkat beserta relasi foto dan daftar tarif shift dari Supabase.
    suspend fun getDevicesWithDetails(): Result<List<DeviceDetailsModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val columns = Columns.raw("*, device_images(*), device_rates(*)")
                val data = SupabaseClient.client
                    .from(devicesTable)
                    .select(columns = columns) {
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<DeviceDetailsModel>()

                Result.Success(data)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat daftar perangkat")
            }
        }
    }

    // Method ini digunakan untuk mengambil detail satu perangkat berdasarkan ID beserta seluruh data foto dan tarif yang terkait.
    suspend fun getDeviceByIdWithDetails(deviceId: String): Result<DeviceDetailsModel> {
        return withContext(Dispatchers.IO) {
            try {
                val columns = Columns.raw("*, device_images(*), device_rates(*)")
                val data = SupabaseClient.client
                    .from(devicesTable)
                    .select(columns = columns) {
                        filter { eq("id", deviceId) }
                    }
                    .decodeSingle<DeviceDetailsModel>()

                Result.Success(data)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat detail perangkat")
            }
        }
    }

    // Method ini digunakan untuk menambahkan perangkat baru ke database sekaligus mencatat aktivitas penambahannya ke tabel device_logs.
    suspend fun addDevice(device: DeviceModel, actorId: String? = null): Result<DeviceModel> {
        return withContext(Dispatchers.IO) {
            try {
                val inserted = SupabaseClient.client.from("devices")
                    .insert(device) { select() }
                    .decodeSingle<DeviceModel>()

                inserted.id?.let { newId ->
                    insertDeviceLog(
                        deviceId = newId,
                        actorId = actorId,
                        fromStatus = "new",
                        toStatus = inserted.status ?: "available",
                        note = "Unit baru ditambahkan"
                    )
                }
                Result.Success(inserted)
            } catch (e: Exception) {
                val message = if (e.message?.contains("23505") == true || e.message?.contains("unique", true) == true) {
                    "Serial Number konsol atau TV sudah digunakan perangkat lain"
                } else {
                    e.message ?: "Gagal menambahkan perangkat"
                }
                Result.Error(message)
            }
        }
    }

    // Method ini digunakan untuk memperbarui informasi perangkat dan mencatat perubahan status ke tabel device_logs jika status mengalami perubahan.
    suspend fun updateDevice(
        deviceId: String,
        device: DeviceModel,
        oldStatus: String,
        actorId: String? = null,
        statusNote: String? = null
    ): Result<DeviceModel> {
        return withContext(Dispatchers.IO) {
            try {
                val newStatus = device.status ?: "available"

                val result = SupabaseClient.client
                    .from(devicesTable)
                    .update({
                        set("name", device.name)
                        set("consol_type", device.consoleType)
                        set("console_sn", device.consoleSn)
                        set("tv_name", device.tvName)
                        set("tv_sn", device.tvSn)
                        set("description", device.description)
                        set("tv_ip_address", device.tvIpAddress)
                        set("tv_mac_address", device.tvMacAddress)
                        set("status", newStatus)
                    }) {
                        filter { eq("id", deviceId) }
                        select()
                    }
                    .decodeSingle<DeviceModel>()

                // Catat otomatis ke device_logs hanya jika status perangkat berubah
                if (!oldStatus.equals(newStatus, ignoreCase = true)) {
                    insertDeviceLog(
                        deviceId = deviceId,
                        actorId = actorId,
                        fromStatus = oldStatus,
                        toStatus = newStatus,
                        note = statusNote?.ifBlank { null } ?: "Status diubah oleh Owner"
                    )
                }

                Result.Success(result)
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

    // Method ini digunakan untuk menghapus data perangkat berdasarkan ID sekaligus membersihkan file foto terkait dari Supabase Storage.
    suspend fun deleteDevice(deviceId: String?): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (deviceId.isNullOrBlank()) {
                    return@withContext Result.Error("ID perangkat tidak valid")
                }

                // Membersihkan file Storage terlebih dahulu sebelum baris perangkat dihapus.
                runCatching {
                    val images = SupabaseClient.client
                        .from(deviceImagesTable)
                        .select {
                            filter { eq("device_id", deviceId) }
                        }
                        .decodeList<DeviceImageModel>()

                    images.forEach { image ->
                        image.storagePath?.let { path ->
                            storageRepository.deleteImage(deviceImagesBucket, path)
                        }
                    }
                }

                SupabaseClient.client
                    .from(devicesTable)
                    .delete {
                        filter { eq("id", deviceId) }
                    }

                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal menghapus perangkat")
            }
        }
    }

    // Manajemen foto perangkat pada tabel device_images dan bucket device-images

    // Method ini digunakan untuk mengunggah foto perangkat ke Supabase Storage dan menyimpan informasi foto tersebut ke tabel device_images.
    suspend fun addDeviceImage(
        deviceId: String,
        imageBytes: ByteArray,
        isPrimary: Boolean = true
    ): Result<DeviceImageModel> {
        return withContext(Dispatchers.IO) {
            var uploadedPath: String? = null
            try {
                val uploadResult = storageRepository.uploadImage(
                    bucketName = deviceImagesBucket,
                    folderPrefix = "device_$deviceId",
                    bytes = imageBytes
                )

                if (uploadResult is Result.Error) {
                    return@withContext Result.Error(uploadResult.message)
                }

                val uploaded = (uploadResult as Result.Success).data
                uploadedPath = uploaded.storagePath

                if (isPrimary) {
                    SupabaseClient.client
                        .from(deviceImagesTable)
                        .update({
                            set("is_primary", false)
                        }) {
                            filter { eq("device_id", deviceId) }
                        }
                }

                val newImage = DeviceImageModel(
                    id = null,
                    deviceId = deviceId,
                    imageUrl = uploaded.publicUrl,
                    storagePath = uploaded.storagePath,
                    isPrimary = isPrimary
                )

                val saved = SupabaseClient.client
                    .from(deviceImagesTable)
                    .insert(newImage) { select() }
                    .decodeSingle<DeviceImageModel>()

                Result.Success(saved)
            } catch (e: Exception) {
                // Menghapus file Storage jika penyimpanan metadata ke database gagal.
                uploadedPath?.let { path ->
                    runCatching { storageRepository.deleteImage(deviceImagesBucket, path) }
                }
                Result.Error(e.message ?: "Gagal menyimpan foto perangkat")
            }
        }
    }

    // Method ini digunakan untuk menghapus data foto berdasarkan ID dari database sekaligus menghapus file fisiknya dari Supabase Storage.
    suspend fun deleteDeviceImage(
        imageId: String,
        storagePath: String?
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!storagePath.isNullOrBlank()) {
                    storageRepository.deleteImage(deviceImagesBucket, storagePath)
                }

                SupabaseClient.client
                    .from(deviceImagesTable)
                    .delete {
                        filter { eq("id", imageId) }
                    }

                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal menghapus foto perangkat")
            }
        }
    }

    // Manajemen tarif berbasis waktu pada tabel device_rates

    // Method ini digunakan untuk menambahkan tarif shift baru ke tabel device_rates tanpa menonaktifkan tarif lainnya.
    suspend fun addDeviceRate(rate: DeviceRateModel): Result<DeviceRateModel> {
        return withContext(Dispatchers.IO) {
            try {
                val newRate = rate.copy(id = null)
                val saved = SupabaseClient.client
                    .from(deviceRatesTable)
                    .insert(newRate) { select() }
                    .decodeSingle<DeviceRateModel>()

                Result.Success(saved)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal menambahkan tarif")
            }
        }
    }

    // Method ini digunakan untuk menyimpan riwayat perubahan status perangkat ke tabel device_logs sebagai catatan aktivitas.
    private suspend fun insertDeviceLog(
        deviceId: String,
        actorId: String?,
        fromStatus: String,
        toStatus: String,
        note: String
    ) {
        try {
            SupabaseClient.client.from("device_logs").insert(
                mapOf(
                    "device_id" to deviceId,
                    "actor_id" to actorId,
                    "from_status" to fromStatus,
                    "to_status" to toStatus,
                    "note" to note
                )
            )
        } catch (_: Exception) {
            // Non-blocking audit log
        }
    }

    // Method ini digunakan untuk memperbarui informasi tarif perangkat seperti nama shift, harga per jam, rentang waktu, dan status aktif.
    suspend fun updateDeviceRate(
        rateId: String,
        rate: DeviceRateModel
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client
                    .from(deviceRatesTable)
                    .update({
                        set("rate_name", rate.rateName)
                        set("price_per_hour", rate.pricePerHour)
                        set("effective_from", rate.effectiveFrom)
                        set("effective_until", rate.effectiveUntil)
                        set("is_active", rate.isActive)
                    }) {
                        filter { eq("id", rateId) }
                    }

                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memperbarui tarif")
            }
        }
    }

    // Method ini digunakan untuk menghapus satu data tarif perangkat dari tabel device_rates berdasarkan ID tarif.
    suspend fun deleteDeviceRate(rateId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client
                    .from(deviceRatesTable)
                    .delete {
                        filter { eq("id", rateId) }
                    }

                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal menghapus tarif")
            }
        }
    }
}