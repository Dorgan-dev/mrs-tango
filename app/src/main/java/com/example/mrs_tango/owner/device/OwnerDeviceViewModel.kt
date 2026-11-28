package com.example.mrs_tango.owner.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.DeviceLogModel
import com.example.mrs_tango.core.data.model.DeviceModel
import com.example.mrs_tango.core.data.model.DeviceRateModel
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.core.data.repository.OwnerDeviceRepository
import com.example.mrs_tango.core.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Class ini digunakan untuk merepresentasikan tiga kondisi proses pemuatan daftar perangkat, yaitu Loading, Success, dan Error.
sealed class OwnerDeviceListState {
    object Loading : OwnerDeviceListState()
    data class Success(val devices: List<DeviceDetailsModel>) : OwnerDeviceListState()
    data class Error(val message: String) : OwnerDeviceListState()
}

// Class ini digunakan untuk merepresentasikan kondisi proses tindakan perangkat, seperti Idle, Loading, Success, dan Error.
sealed class OwnerDeviceActionState {
    object Idle : OwnerDeviceActionState()
    object Loading : OwnerDeviceActionState()
    data class Success(val message: String) : OwnerDeviceActionState()
    data class Error(val message: String) : OwnerDeviceActionState()
}

class OwnerDeviceViewModel(
    private val repository: OwnerDeviceRepository = OwnerDeviceRepository()
) : ViewModel() {

    private val _devicesState = MutableStateFlow<OwnerDeviceListState>(OwnerDeviceListState.Loading)
    val devicesState: StateFlow<OwnerDeviceListState> = _devicesState.asStateFlow()

    private val _actionState = MutableStateFlow<OwnerDeviceActionState>(OwnerDeviceActionState.Idle)
    val actionState: StateFlow<OwnerDeviceActionState> = _actionState.asStateFlow()

    private val _logsState = MutableStateFlow<List<DeviceLogModel>>(emptyList())
    val logsState: StateFlow<List<DeviceLogModel>> = _logsState.asStateFlow()

    private var currentOwnerId: String? = null

    // Method ini digunakan untuk mengambil daftar perangkat dari repository dan memperbarui state sesuai hasil pemuatan data.
    fun fetchDevices(ownerId: String? = null) {
        if (ownerId != null) {
            this.currentOwnerId = ownerId
        }
        viewModelScope.launch {
            _devicesState.value = OwnerDeviceListState.Loading
            when (val res = repository.getAllDevices()) {
                is Result.Success -> _devicesState.value = OwnerDeviceListState.Success(res.data)
                is Result.Error -> _devicesState.value = OwnerDeviceListState.Error(res.message)
                else -> {}
            }
        }
    }

    // Method ini digunakan untuk menambahkan perangkat baru atau memperbarui perangkat yang sudah ada berdasarkan ID perangkat.
    fun saveDevice(
        existingId: String?,
        oldStatus: String,
        device: DeviceModel,
        actorId: String?,
        statusNote: String?
    ) {
        viewModelScope.launch {
            _actionState.value = OwnerDeviceActionState.Loading
            val res = if (existingId == null) {
                repository.createDevice(device, actorId)
            } else {
                repository.updateDevice(existingId, device, oldStatus, actorId, statusNote)
            }

            when (res) {
                is Result.Success -> {
                    _actionState.value = OwnerDeviceActionState.Success(
                        if (existingId == null) "Perangkat berhasil ditambahkan" else "Perangkat diperbarui"
                    )
                    fetchDevices(currentOwnerId)
                }
                is Result.Error -> _actionState.value = OwnerDeviceActionState.Error(res.message)
                else -> {}
            }
        }
    }

    // Method ini digunakan untuk menghapus perangkat beserta seluruh foto terkait melalui repository dan memuat ulang daftar perangkat.
    fun deleteDevice(device: DeviceDetailsModel) {
        viewModelScope.launch {
            _actionState.value = OwnerDeviceActionState.Loading
            val paths = device.images?.map { it.storagePath }
            when (val res = repository.deleteDevice(device.id, paths)) {
                is Result.Success -> {
                    _actionState.value = OwnerDeviceActionState.Success("Perangkat berhasil dihapus")
                    fetchDevices(currentOwnerId)
                }
                is Result.Error -> _actionState.value = OwnerDeviceActionState.Error(res.message)
                else -> {}
            }
        }
    }

    // Method ini digunakan untuk mengunggah foto perangkat ke repository dengan menentukan foto utama dan urutan tampilnya.
    fun uploadPhoto(deviceId: String, bytes: ByteArray, isPrimary: Boolean, sortOrder: Int) {
        viewModelScope.launch {
            _actionState.value = OwnerDeviceActionState.Loading
            when (val res = repository.addDeviceImage(deviceId, bytes, isPrimary, sortOrder)) {
                is Result.Success -> {
                    _actionState.value = OwnerDeviceActionState.Success("Foto berhasil ditambahkan")
                    fetchDevices(currentOwnerId)
                }
                is Result.Error -> _actionState.value = OwnerDeviceActionState.Error(res.message)
                else -> {}
            }
        }
    }

    // Method ini digunakan untuk mengubah foto perangkat yang dipilih menjadi foto utama dan memperbarui daftar perangkat.
    fun setPrimaryPhoto(deviceId: String, imageId: String) {
        viewModelScope.launch {
            when (val res = repository.setPrimaryImage(deviceId, imageId)) {
                is Result.Success -> {
                    _actionState.value = OwnerDeviceActionState.Success("Foto utama diubah")
                    fetchDevices(currentOwnerId)
                }
                is Result.Error -> _actionState.value = OwnerDeviceActionState.Error(res.message)
                else -> {}
            }
        }
    }

    // Method ini digunakan untuk menghapus foto perangkat berdasarkan ID foto dan lokasi penyimpanannya di Storage.
    fun deletePhoto(imageId: String, storagePath: String) {
        viewModelScope.launch {
            when (val res = repository.deleteDeviceImage(imageId, storagePath)) {
                is Result.Success -> {
                    _actionState.value = OwnerDeviceActionState.Success("Foto dihapus")
                    fetchDevices(currentOwnerId)
                }
                is Result.Error -> _actionState.value = OwnerDeviceActionState.Error(res.message)
                else -> {}
            }
        }
    }

    // Method ini digunakan untuk menambahkan tarif shift baru ke perangkat melalui repository dan memuat ulang daftar perangkat.
    fun addRate(rate: DeviceRateModel) {
        viewModelScope.launch {
            _actionState.value = OwnerDeviceActionState.Loading
            when (val res = repository.addDeviceRate(rate)) {
                is Result.Success -> {
                    _actionState.value = OwnerDeviceActionState.Success("Shift tarif ditambahkan")
                    fetchDevices(currentOwnerId)
                }
                is Result.Error -> _actionState.value = OwnerDeviceActionState.Error(res.message)
                else -> {}
            }
        }
    }

    // Method ini digunakan untuk mengaktifkan atau menonaktifkan tarif perangkat berdasarkan ID tarif yang dipilih.
    fun toggleRate(rateId: String, isActive: Boolean) {
        viewModelScope.launch {
            when (val res = repository.toggleRateActive(rateId, isActive)) {
                is Result.Success -> fetchDevices(currentOwnerId)
                is Result.Error -> _actionState.value = OwnerDeviceActionState.Error(res.message)
                else -> {}
            }
        }
    }

    // Method ini digunakan untuk menghapus tarif perangkat berdasarkan ID tarif dan memperbarui daftar perangkat setelah berhasil.
    fun deleteRate(rateId: String) {
        viewModelScope.launch {
            when (val res = repository.deleteDeviceRate(rateId)) {
                is Result.Success -> {
                    _actionState.value = OwnerDeviceActionState.Success("Tarif dihapus")
                    fetchDevices(currentOwnerId)
                }
                is Result.Error -> _actionState.value = OwnerDeviceActionState.Error(res.message)
                else -> {}
            }
        }
    }

    // Method ini digunakan untuk mengambil riwayat aktivitas perangkat dari repository dan menyimpannya ke logsState.
    fun fetchLogs() {
        viewModelScope.launch {
            val res = repository.getRecentDeviceLogs()
            if (res is Result.Success) {
                _logsState.value = res.data
            }
        }
    }

    // Method ini digunakan untuk mengembalikan actionState ke kondisi Idle setelah proses tindakan selesai ditangani.
    fun resetActionState() {
        _actionState.value = OwnerDeviceActionState.Idle
    }
}