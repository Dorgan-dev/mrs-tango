package com.example.mrs_tango.customer.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.BookingModel
import com.example.mrs_tango.core.data.repository.BookingRepository
import com.example.mrs_tango.core.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BookingHistoryState {
    object Loading : BookingHistoryState()
    data class Success(val bookings: List<BookingModel>) : BookingHistoryState()
    data class Error(val message: String) : BookingHistoryState()
}

sealed class BookingActionState {
    object Idle : BookingActionState()
    object Loading : BookingActionState()
    data class Success(val message: String) : BookingActionState()
    data class Error(val message: String) : BookingActionState()
}

class BookingViewModel : ViewModel() {

    private val repository = BookingRepository()

    private val _historyState = MutableStateFlow<BookingHistoryState>(BookingHistoryState.Loading)
    val historyState: StateFlow<BookingHistoryState> = _historyState

    private val _actionState = MutableStateFlow<BookingActionState>(BookingActionState.Idle)
    val actionState: StateFlow<BookingActionState> = _actionState

    fun fetchCustomerBookings(customerId: String) {
        _historyState.value = BookingHistoryState.Loading
        viewModelScope.launch {
            when (val result = repository.getCustomerBookings(customerId)) {
                is Result.Success -> {
                    _historyState.value = BookingHistoryState.Success(result.data)
                }
                is Result.Error -> {
                    _historyState.value = BookingHistoryState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun createBooking(booking: BookingModel) {
        if (booking.usageDate.isBlank() || booking.startTime.isBlank()) {
            _actionState.value = BookingActionState.Error("Tanggal dan jam mulai wajib diisi")
            return
        }

        _actionState.value = BookingActionState.Loading
        viewModelScope.launch {
            when (val result = repository.createBooking(booking)) {
                is Result.Success -> {
                    _actionState.value = BookingActionState.Success("Booking berhasil dikirim! Menunggu konfirmasi.")
                }
                is Result.Error -> {
                    _actionState.value = BookingActionState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun cancelBooking(bookingId: String, reason: String, customerId: String) {
        if (reason.isBlank()) {
            _actionState.value = BookingActionState.Error("Alasan pembatalan wajib diisi")
            return
        }

        _actionState.value = BookingActionState.Loading
        viewModelScope.launch {
            when (val result = repository.cancelBooking(bookingId, reason, customerId)) {
                is Result.Success -> {
                    _actionState.value = BookingActionState.Success("Booking berhasil dibatalkan")
                    fetchCustomerBookings(customerId) // Refresh riwayat otomatis
                }
                is Result.Error -> {
                    _actionState.value = BookingActionState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun resetActionState() {
        _actionState.value = BookingActionState.Idle
    }
}