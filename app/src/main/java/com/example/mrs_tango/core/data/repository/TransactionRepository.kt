package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.TransactionWithDetails
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionRepository {

    suspend fun getCustomerTransactions(customerId: String): Result<List<TransactionWithDetails>> {
        return withContext(Dispatchers.IO) {
            try {
                val columns = Columns.raw(
                    "*, play_sessions!inner(customer_id, started_at, ended_at, duration_minutes, devices(name, consol_type)), transaction_items(*)"
                )
                val list = SupabaseClient.client.from("transactions")
                    .select(columns = columns) {
                        filter {
                            eq("play_sessions.customer_id", customerId)
                        }
                        order(column = "paid_at", order = Order.DESCENDING)
                    }
                    .decodeList<TransactionWithDetails>()

                Result.Success(list)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat riwayat transaksi")
            }
        }
    }
}