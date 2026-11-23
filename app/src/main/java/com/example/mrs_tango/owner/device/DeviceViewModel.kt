package com.example.mrs_tango.owner.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.DeviceModel
import com.example.mrs_tango.core.data.repository.DeviceRepository
import com.example.mrs_tango.core.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DeviceListState {
    object Loading : DeviceListState()
    data class Success(val devices: List<DeviceModel>) : DeviceListState()
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
    val listState: StateFlow<DeviceListState> = _listState

    private val _actionState = MutableStateFlow<DeviceActionState>(DeviceActionState.Idle)
    val actionState: StateFlow<DeviceActionState> = _actionState

    private var fullDeviceList: List<DeviceModel> = emptyList() // Menyimpan daftar asli untuk fitur pencarian lokal

    init {
        fetchDevices()
    }

    fun fetchDevices() {
        _listState.value = DeviceListState.Loading
        viewModelScope.launch {
            when (val result = repository.getAllDevices()) {
                is Result.Success -> {
                    fullDeviceList = result.data
                    _listState.value = DeviceListState.Success(fullDeviceList)
                }
                is Result.Error -> {
                    _listState.value = DeviceListState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun searchDevices(query: String) {
        if (query.isBlank()) {
            _listState.value = DeviceListState.Success(fullDeviceList)
            return
        }
        val filtered = fullDeviceList.filter { device ->
            (device.name?.contains(query, ignoreCase = true) == true) ||
                    (device.consoleType?.contains(query, ignoreCase = true) == true) ||
                    (device.tvIpAddress?.contains(query, ignoreCase = true) == true)
        }
        _listState.value = DeviceListState.Success(filtered)
    }

    fun addDevice(device: DeviceModel) {
        if (device.name.isNullOrBlank()) {
            _actionState.value = DeviceActionState.Error("Nama unit wajib diisi")
            return
        }

        _actionState.value = DeviceActionState.Loading
        viewModelScope.launch {
            when (val result = repository.addDevice(device)) {
                is Result.Success -> {
                    _actionState.value = DeviceActionState.Success("Perangkat berhasil ditambahkan")
                    fetchDevices() // Refresh daftar otomatis
                }
                is Result.Error -> {
                    _actionState.value = DeviceActionState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun updateDevice(deviceId: String, device: DeviceModel) {
        if (device.name.isNullOrBlank()) {
            _actionState.value = DeviceActionState.Error("Nama unit wajib diisi")
            return
        }

        _actionState.value = DeviceActionState.Loading
        viewModelScope.launch {
            when (val result = repository.updateDevice(deviceId, device)) {
                is Result.Success -> {
                    _actionState.value = DeviceActionState.Success("Perangkat berhasil diperbarui")
                    fetchDevices() // Refresh daftar otomatis
                }
                is Result.Error -> {
                    _actionState.value = DeviceActionState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteDevice(deviceId: String) {
        _actionState.value = DeviceActionState.Loading
        viewModelScope.launch {
            when (val result = repository.deleteDevice(deviceId)) {
                is Result.Success -> {
                    _actionState.value = DeviceActionState.Success("Perangkat berhasil dihapus")
                    fetchDevices() // Refresh daftar otomatis
                }
                is Result.Error -> {
                    _actionState.value = DeviceActionState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun resetActionState() {
        _actionState.value = DeviceActionState.Idle
    }
}