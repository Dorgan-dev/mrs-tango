package com.example.mrs_tango.owner.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.core.data.model.DeviceModel
import com.example.mrs_tango.core.data.repository.DeviceRepository
import com.example.mrs_tango.core.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DeviceListState {
    object Loading : DeviceListState()
    data class Success(val devices: List<DeviceDetailsModel>) : DeviceListState()
    data class Error(val message: String) : DeviceListState()
}

sealed class DeviceActionState {
    object Idle : DeviceActionState()
    object Loading : DeviceActionState()
    data class Success(val message: String) : DeviceActionState()
    data class Error(val message: String) : DeviceActionState()
}

class DeviceViewModel : ViewModel() {

    private val repository = DeviceRepository()

    private val _listState = MutableStateFlow<DeviceListState>(DeviceListState.Loading)
    val listState: StateFlow<DeviceListState> = _listState.asStateFlow()

    private val _actionState = MutableStateFlow<DeviceActionState>(DeviceActionState.Idle)
    val actionState: StateFlow<DeviceActionState> = _actionState.asStateFlow()

    private var fullDeviceList: List<DeviceDetailsModel> = emptyList()

    init {
        fetchDevices()
    }

    // Mengambil daftar lengkap perangkat beserta detailnya dari repository dan memperbarui listState.
    fun fetchDevices() {
        _listState.value = DeviceListState.Loading
        viewModelScope.launch {
            when (val result = repository.getDevicesWithDetails()) {
                is Result.Success -> {
                    fullDeviceList = result.data
                    _listState.value = DeviceListState.Success(fullDeviceList)
                }
                is Result.Error -> _listState.value = DeviceListState.Error(result.message)
                else -> Unit
            }
        }
    }

    // Menyaring daftar perangkat di memori berdasarkan nama, tipe konsol, atau alamat IP TV.
    fun searchDevices(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _listState.value = DeviceListState.Success(fullDeviceList)
            return
        }

        val filtered = fullDeviceList.filter { device ->
            listOf(device.name, device.consoleType, device.tvIpAddress).any { field ->
                field.orEmpty().contains(trimmed, ignoreCase = true)
            }
        }
        _listState.value = DeviceListState.Success(filtered)
    }

    // Menambahkan perangkat baru setelah memvalidasi nama unit lalu memuat ulang daftar perangkat.
    fun addDevice(device: DeviceModel, actorId: String? = null) {
        if (!isDeviceNameValid(device)) return

        performAction("Perangkat berhasil ditambahkan") {
            repository.addDevice(device, actorId)
        }
    }

    // Memperbarui data perangkat yang sudah ada setelah memvalidasi nama unit lalu menyegarkan daftar.
    fun updateDevice(
        deviceId: String,
        device: DeviceModel,
        oldStatus: String = "available",
        actorId: String? = null,
        statusNote: String? = null
    ) {
        if (!isDeviceNameValid(device)) return

        performAction("Perangkat berhasil diperbarui") {
            repository.updateDevice(deviceId, device, oldStatus, actorId, statusNote)
        }
    }

    // Menghapus perangkat berdasarkan ID dan memperbarui status aksi beserta daftar perangkat.
    fun deleteDevice(deviceId: String?) {
        performAction("Perangkat berhasil dihapus") {
            repository.deleteDevice(deviceId)
        }
    }

    // Mengembalikan actionState ke kondisi Idle setelah pesan status atau error ditangani UI.
    fun resetActionState() {
        _actionState.value = DeviceActionState.Idle
    }

    // Memeriksa apakah nama unit terisi dan menampilkan pesan error jika kosong.
    private fun isDeviceNameValid(device: DeviceModel): Boolean {
        if (device.name.isNullOrBlank()) {
            _actionState.value = DeviceActionState.Error("Nama unit wajib diisi")
            return false
        }
        return true
    }

    // Menjalankan operasi mutasi repository secara terpusat dan memicu fetchDevices saat sukses.
    private fun performAction(
        successMessage: String,
        block: suspend () -> Result<Any>
    ) {
        _actionState.value = DeviceActionState.Loading
        viewModelScope.launch {
            when (val result = block()) {
                is Result.Success -> {
                    _actionState.value = DeviceActionState.Success(successMessage)
                    fetchDevices()
                }
                is Result.Error -> _actionState.value = DeviceActionState.Error(result.message)
                else -> Unit
            }
        }
    }
}