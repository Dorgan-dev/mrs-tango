package com.example.mrs_tango.customer.booking.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.BookingDetailsModel
import com.example.mrs_tango.core.data.repository.BookingRepository
import com.example.mrs_tango.core.utils.Result
import com.example.mrs_tango.customer.booking.BookingActionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookingHistoryState {
    object Loading : BookingHistoryState()
    data class Success(val bookings: List<BookingDetailsModel>) : BookingHistoryState()
    data class Error(val message: String) : BookingHistoryState()
}

class BookingHistoryViewModel(
    private val bookingRepository: BookingRepository = BookingRepository()
) : ViewModel() {

    private val _historyState =
        MutableStateFlow<BookingHistoryState>(BookingHistoryState.Loading)
    val historyState = _historyState.asStateFlow()

    private val _actionState =
        MutableStateFlow<BookingActionState>(BookingActionState.Idle)
    val actionState = _actionState.asStateFlow()

    private var allBookingsCache: List<BookingDetailsModel> = emptyList()
    private var currentFilter = "ALL"

    // Mengambil seluruh riwayat booking pelanggan dan menerapkan filter yang sedang aktif.
    fun fetchBookings(customerId: String) {
        viewModelScope.launch {
            _historyState.value = BookingHistoryState.Loading

            when (val result = bookingRepository.getCustomerBookingsWithDetails(customerId)) {
                is Result.Success -> {
                    allBookingsCache = result.data
                    applyFilter(currentFilter)
                }
                is Result.Error -> {
                    _historyState.value = BookingHistoryState.Error(result.message)
                }
                else -> Unit
            }
        }
    }

    // Menyaring daftar booking berdasarkan kategori aktif, riwayat, atau seluruh pesanan.
    fun applyFilter(filterType: String) {
        currentFilter = filterType

        val activeStatuses = setOf("pending", "confirmed")
        val filtered = when (filterType) {
            "ACTIVE" -> allBookingsCache.filter { it.status in activeStatuses }
            "HISTORY" -> allBookingsCache.filter { it.status !in activeStatuses }
            else -> allBookingsCache
        }

        _historyState.value = BookingHistoryState.Success(filtered)
    }

    // Membatalkan booking pelanggan dan memperbarui daftar riwayat setelah berhasil.
    fun cancelBooking(customerId: String, bookingId: String, reason: String) {
        viewModelScope.launch {
            _actionState.value = BookingActionState.Loading

            when (val result = bookingRepository.cancelBooking(bookingId, reason)) {
                is Result.Success -> {
                    _actionState.value = BookingActionState.Success(
                        "Pesanan berhasil dibatalkan"
                    )
                    fetchBookings(customerId)
                }
                is Result.Error -> {
                    _actionState.value = BookingActionState.Error(result.message)
                }
                else -> Unit
            }
        }
    }

    // Mengunggah ulang bukti pembayaran DP dan memperbarui riwayat booking setelah berhasil.
    fun reuploadDpProof(
        customerId: String,
        bookingId: String,
        newBytes: ByteArray,
        oldUrl: String?
    ) {
        viewModelScope.launch {
            _actionState.value = BookingActionState.Loading

            when (val result = bookingRepository.updateBookingDpProof(
                bookingId,
                customerId,
                newBytes,
                oldUrl
            )) {
                is Result.Success -> {
                    _actionState.value = BookingActionState.Success(
                        "Bukti transfer DP berhasil diperbarui"
                    )
                    fetchBookings(customerId)
                }
                is Result.Error -> {
                    _actionState.value = BookingActionState.Error(result.message)
                }
                else -> Unit
            }
        }
    }

    // Mengembalikan status aksi booking ke kondisi awal.
    fun resetActionState() {
        _actionState.value = BookingActionState.Idle
    }
}