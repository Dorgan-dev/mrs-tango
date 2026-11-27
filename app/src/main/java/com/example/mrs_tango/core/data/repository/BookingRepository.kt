package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.BookingDetailsModel
import com.example.mrs_tango.core.data.model.BookingModel
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookingRepository(
    private val storageRepository: StorageRepository = StorageRepository()
) {

    // Mengunggah bukti pembayaran DP ke Storage dan menyimpan data booking beserta URL bukti pembayaran ke database.
    suspend fun createBookingWithDp(
        booking: BookingModel,
        dpProofBytes: ByteArray
    ): Result<BookingModel> {
        return withContext(Dispatchers.IO) {
            try {
                val uploadResult = storageRepository.uploadImage(
                    bucketName = "dp-proofs",
                    folderPrefix = "dp_${booking.customerId}",
                    bytes = dpProofBytes
                )

                if (uploadResult is Result.Error) {
                    return@withContext Result.Error(
                        "Gagal unggah bukti DP: ${uploadResult.message}"
                    )
                }

                val proofUrl = (uploadResult as Result.Success).data.publicUrl
                val finalBooking = booking.copy(dpProofUrl = proofUrl)

                val inserted = SupabaseClient.client.from("bookings")
                    .insert(finalBooking) { select() }
                    .decodeSingle<BookingModel>()

                Result.Success(inserted)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal membuat booking")
            }
        }
    }

    // Mengambil seluruh riwayat booking milik pelanggan berdasarkan ID dan mengurutkannya dari yang terbaru.
    suspend fun getCustomerBookings(customerId: String): Result<List<BookingModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val list = SupabaseClient.client.from("bookings")
                    .select {
                        filter { eq("user_id", customerId) }
                        order(column = "created_at", order = Order.DESCENDING)
                    }
                    .decodeList<BookingModel>()

                Result.Success(list)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat riwayat booking")
            }
        }
    }

    // Memperbarui status booking menjadi cancelled serta menyimpan alasan dan pihak yang melakukan pembatalan.
    suspend fun cancelBooking(
        bookingId: String,
        reason: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.from("bookings").update(
                    mapOf(
                        "status" to "cancelled",
                        "cancel_reason" to reason,
                        "cancelled_by" to "customer"
                    )
                ) {
                    filter { eq("id", bookingId) }
                }

                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal membatalkan booking")
            }
        }
    }

    // Ambil riwayat booking lengkap dengan nama device & info refund
    suspend fun getCustomerBookingsWithDetails(customerId: String): Result<List<BookingDetailsModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                    "*, devices(name, consol_type), refunds(*)"
                )
                val list = SupabaseClient.client.from("bookings")
                    .select(columns = columns) {
                        filter { eq("customer_id", customerId) }
                        order(column = "created_at", order = Order.DESCENDING)
                    }
                    .decodeList<BookingDetailsModel>()
                Result.Success(list)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat riwayat booking")
            }
        }
    }

    // Ganti foto bukti DP (selama status masih pending) & hapus file lama agar hemat storage
    suspend fun updateBookingDpProof(
        bookingId: String,
        customerId: String,
        newProofBytes: ByteArray,
        oldProofUrl: String?
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val uploadResult = storageRepository.uploadImage(
                    bucketName = "dp-proofs",
                    folderPrefix = "dp_$customerId",
                    bytes = newProofBytes
                )
                if (uploadResult is Result.Error) {
                    return@withContext Result.Error(uploadResult.message)
                }
                val newUrl = (uploadResult as Result.Success).data.publicUrl
                SupabaseClient.client.from("bookings").update(
                    mapOf("dp_proof_file" to newUrl)
                ) {
                    filter { eq("id", bookingId) }
                }
                // Hapus bukti transfer lama dari bucket dp-proofs
                if (!oldProofUrl.isNullOrBlank() && oldProofUrl.contains("/dp-proofs/")) {
                    val oldFileName = oldProofUrl.substringAfter("/dp-proofs/")
                    if (oldFileName.isNotBlank()) {
                        storageRepository.deleteImage("dp-proofs", oldFileName)
                    }
                }
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memperbarui bukti transfer DP")
            }
        }
    }
}