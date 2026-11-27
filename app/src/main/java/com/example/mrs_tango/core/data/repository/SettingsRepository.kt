package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.SettingModel
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepository {

    // Mengambil seluruh pengaturan toko dari tabel settings dan mengubahnya menjadi Map dengan key dan value.
    suspend fun getAllSettingsMap(): Result<Map<String, String>> {
        return withContext(Dispatchers.IO) {
            try {
                val list = SupabaseClient.client.from("settings")
                    .select()
                    .decodeList<SettingModel>()

                val map = list.associate { it.key to (it.value ?: "") }
                Result.Success(map)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat pengaturan toko")
            }
        }
    }
}