package com.example.mrs_tango.customer.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.BookingModel
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.core.data.repository.BookingRepository
import com.example.mrs_tango.core.data.repository.DeviceRepository
import com.example.mrs_tango.core.data.repository.SettingsRepository
import com.example.mrs_tango.core.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookingFormInitState {
    object Loading : BookingFormInitState()
    data class Ready(
        val device: DeviceDetailsModel,
        val storeName: String,
        val qrisString: String
    ) : BookingFormInitState()
    data class Error(val message: String) : BookingFormInitState()
}

sealed class BookingActionState {
    object Idle : BookingActionState()
    object Loading : BookingActionState()
    data class Success(val message: String) : BookingActionState()
    data class Error(val message: String) : BookingActionState()
}

class BookingViewModel(
    private val bookingRepository: BookingRepository = BookingRepository(),
    private val deviceRepository: DeviceRepository = DeviceRepository(),
    private val settingsRepository: SettingsRepository = SettingsRepository()
) : ViewModel() {

    private val _formInitState =
        MutableStateFlow<BookingFormInitState>(BookingFormInitState.Loading)
    val formInitState: StateFlow<BookingFormInitState> = _formInitState.asStateFlow()

    private val _actionState =
        MutableStateFlow<BookingActionState>(BookingActionState.Idle)
    val actionState: StateFlow<BookingActionState> = _actionState.asStateFlow()

    // Memuat informasi perangkat dan pengaturan toko sebagai data awal formulir booking.
    fun loadBookingFormData(deviceId: String) {
        viewModelScope.launch {
            _formInitState.value = BookingFormInitState.Loading
            val deviceRes = deviceRepository.getDeviceByIdWithDetails(deviceId)
            val settingsRes = settingsRepository.getAllSettingsMap()
            if (deviceRes is Result.Error) {
                _formInitState.value =
                    BookingFormInitState.Error(deviceRes.message)
                return@launch
            }
            val device = (deviceRes as Result.Success<DeviceDetailsModel>).data
            val settings = when (settingsRes) {
                is Result.Success -> settingsRes.data
                else -> emptyMap()
            }
            _formInitState.value = BookingFormInitState.Ready(
                device = device,
                storeName = settings["store_name"] ?: "IZI PLAYSTATION",
                qrisString = settings["qris_string"] ?: ""
            )
        }
    }

    // Mengirim data booking beserta bukti pembayaran DP ke repository.
    fun submitBooking(booking: BookingModel, dpProofBytes: ByteArray) {
        viewModelScope.launch {
            _actionState.value = BookingActionState.Loading
            when (val result = bookingRepository.createBookingWithDp(booking, dpProofBytes)) {
                is Result.Success -> {
                    _actionState.value = BookingActionState.Success(
                        "Booking berhasil diajukan! Menunggu verifikasi kasir maksimal 15 menit."
                    )
                }
                is Result.Error -> {
                    _actionState.value = BookingActionState.Error(result.message)
                }
                else -> Unit
            }
        }
    }

    // Mengembalikan status aksi booking ke kondisi awal setelah hasil aksi selesai ditampilkan.
    fun resetActionState() {
        _actionState.value = BookingActionState.Idle
    }
}