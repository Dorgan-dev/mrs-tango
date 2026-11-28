package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.PlaySessionModel
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionRepository {

    // Mengambil sesi bermain berstatus aktif terbaru milik pelanggan dari Supabase dan membungkus hasilnya ke dalam Result.
    suspend fun getActiveSessionForCustomer(customerId: String): Result<PlaySessionModel?> {
        return withContext(Dispatchers.IO) {
            try {
                val columns = Columns.raw("*, devices(name, consol_type)")
                val sessions = SupabaseClient.client.from("play_sessions")
                    .select(columns = columns) {
                        filter {
                            eq("customer_id", customerId)
                            eq("status", "active")
                        }
                        order(column = "started_at", order = Order.DESCENDING)
                        limit(1)
                    }
                    .decodeList<PlaySessionModel>()

                Result.Success(sessions.firstOrNull())
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memeriksa sesi bermain aktif")
            }
        }
    }
}