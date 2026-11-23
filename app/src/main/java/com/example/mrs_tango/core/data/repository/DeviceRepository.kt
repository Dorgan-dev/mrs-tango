package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.DeviceModel
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceRepository {

    private val tableName = "devices"

    suspend fun getAllDevices(): Result<List<DeviceModel>> { // 1. READ: Ambil semua data perangkat (urutkan dari yang terbaru)
        return withContext(Dispatchers.IO) {
            try {
                val devices = SupabaseClient.client
                    .from(tableName)
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

    suspend fun addDevice(device: DeviceModel): Result<DeviceModel> { // 2. CREATE: Tambah perangkat baru
        return withContext(Dispatchers.IO) {
            try {
                val newDevice = device.copy( // Pastikan id, created_at, updated_at bernilai null agar di-generate otomatis
                    id = null,
                    createdAt = null,
                    updatedAt = null
                )

                val result = SupabaseClient.client
                    .from(tableName)
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

    suspend fun updateDevice(deviceId: String, updatedDevice: DeviceModel): Result<DeviceModel> { // 3. UPDATE: Perbarui data perangkat berdasarkan ID
        return withContext(Dispatchers.IO) {
            try {
                val result = SupabaseClient.client
                    .from(tableName)
                    .update({
                        set("name", updatedDevice.name)
                        set("consol_type", updatedDevice.consoleType)
                        set("console_sn", updatedDevice.consoleSn)
                        set("tv_name", updatedDevice.tvName)
                        set("tv_sn", updatedDevice.tvSn)
                        set("tv_ip_address", updatedDevice.tvIpAddress)
                        set("tv_mac_address", updatedDevice.tvMacAddress)
                        set("status", updatedDevice.status)
                    }) {
                        filter {
                            eq("id", deviceId)
                        }
                        select()
                    }
                    .decodeSingle<DeviceModel>()

                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memperbarui perangkat")
            }
        }
    }

    suspend fun deleteDevice(deviceId: String): Result<Boolean> { // 4. DELETE: Hapus perangkat berdasarkan ID
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client
                    .from(tableName)
                    .delete {
                        filter {
                            eq("id", deviceId)
                        }
                    }

                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal menghapus perangkat")
            }
        }
    }
}