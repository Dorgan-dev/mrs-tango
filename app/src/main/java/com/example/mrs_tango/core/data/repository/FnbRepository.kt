package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.FnbCategoryModel
import com.example.mrs_tango.core.data.model.FnbItemModel
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FnbRepository {

    // Mengambil daftar kategori FnB yang aktif secara alfabetis dari Supabase dan membungkus hasilnya ke dalam Result.
    suspend fun getActiveCategories(): Result<List<FnbCategoryModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val list = SupabaseClient.client.from("fnb_categories")
                    .select {
                        filter { eq("is_active", true) }
                        order(column = "name", order = Order.ASCENDING)
                    }
                    .decodeList<FnbCategoryModel>()
                Result.Success(list)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat kategori FnB")
            }
        }
    }

    // Mengambil seluruh item menu FnB secara alfabetis dari Supabase dan membungkus hasilnya ke dalam Result.
    suspend fun getFnbItems(): Result<List<FnbItemModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val list = SupabaseClient.client.from("fnb_items")
                    .select {
                        order(column = "name", order = Order.ASCENDING)
                    }
                    .decodeList<FnbItemModel>()
                Result.Success(list)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat daftar menu FnB")
            }
        }
    }
}