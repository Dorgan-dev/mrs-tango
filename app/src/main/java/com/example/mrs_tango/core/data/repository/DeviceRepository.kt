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

    // Mengambil seluruh data dasar perangkat dari tabel devices tanpa relasi foto dan tarif.
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

    // Mengambil seluruh perangkat beserta relasi foto dan daftar tarif shift dalam satu query.
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

    // Mengambil detail lengkap perangkat berdasarkan ID beserta galeri foto dan seluruh tarifnya.
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

    // Menambahkan perangkat baru dan mengembalikan data hasil insert termasuk ID UUID dari Supabase.
    suspend fun addDevice(device: DeviceModel): Result<DeviceModel> {
        return withContext(Dispatchers.IO) {
            try {
                val newDevice = device.copy(
                    id = null,
                    createdAt = null,
                    updatedAt = null
                )

                val result = SupabaseClient.client
                    .from(devicesTable)
                    .insert(newDevice) {
                        select()
                    }
                    .decodeSingle<DeviceModel>()

                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal menambahkan perangkat")
            }
        }
    }

    // Memperbarui informasi spesifikasi dan status perangkat berdasarkan ID perangkat.
    suspend fun updateDevice(
        deviceId: String,
        updatedDevice: DeviceModel
    ): Result<DeviceModel> {
        return withContext(Dispatchers.IO) {
            try {
                val result = SupabaseClient.client
                    .from(devicesTable)
                    .update({
                        set("name", updatedDevice.name)
                        set("consol_type", updatedDevice.consoleType)
                        set("console_sn", updatedDevice.consoleSn)
                        set("tv_name", updatedDevice.tvName)
                        set("tv_sn", updatedDevice.tvSn)
                        set("description", updatedDevice.tvSn) //ini kolom baru
                        set("tv_ip_address", updatedDevice.tvIpAddress)
                        set("tv_mac_address", updatedDevice.tvMacAddress)
                        set("status", updatedDevice.status)
                    }) {
                        filter { eq("id", deviceId) }
                        select()
                    }
                    .decodeSingle<DeviceModel>()

                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memperbarui perangkat")
            }
        }
    }

    // Menghapus perangkat berdasarkan ID sekaligus membersihkan seluruh file foto terkait di Supabase Storage.
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

    // Mengunggah foto terkompresi ke Supabase Storage lalu menyimpan URL-nya ke database.
    // Jika isPrimary bernilai true, foto primary sebelumnya akan dinonaktifkan.
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

    // Menghapus satu foto dari database beserta file fisiknya di Storage jika storagePath tersedia.
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

    // Menambahkan satu slot tarif baru tanpa menonaktifkan slot tarif lainnya milik perangkat yang sama.
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

    // Memperbarui nama shift, harga per jam, rentang waktu, atau status aktif berdasarkan ID tarif.
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

    // Menghapus satu slot tarif perangkat dari tabel device_rates berdasarkan ID tarif.
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
