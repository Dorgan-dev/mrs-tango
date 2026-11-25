package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.BookingModel
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookingRepository {

    private val tableName = "bookings"

    // 1. CREATE: Buat pesanan booking baru
    suspend fun createBooking(booking: BookingModel): Result<BookingModel> {
        return withContext(Dispatchers.IO) {
            try {
                val newBooking = booking.copy(
                    id = null,
                    createdAt = null,
                    updatedAt = null
                )

                val result = SupabaseClient.client
                    .from(tableName)
                    .insert(newBooking) {
                        select()
                    }
                    .decodeSingle<BookingModel>()

                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal membuat booking")
            }
        }
    }

    // 2. READ: Ambil riwayat booking berdasarkan customer_id
    suspend fun getCustomerBookings(customerId: String): Result<List<BookingModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val bookings = SupabaseClient.client
                    .from(tableName)
                    .select {
                        filter {
                            eq("customer_id", customerId)
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<BookingModel>()

                Result.Success(bookings)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat riwayat booking")
            }
        }
    }

    // 3. UPDATE (Cancel): Batalkan pesanan oleh pelanggan
    suspend fun cancelBooking(
        bookingId: String,
        reason: String,
        canceledBy: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client
                    .from(tableName)
                    .update({
                        set("status", "canceled")
                        set("cancle_reason", reason)
                        set("canceled_by", canceledBy)
                    }) {
                        filter {
                            eq("id", bookingId)
                        }
                    }

                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal membatalkan booking")
            }
        }
    }
}