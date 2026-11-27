package com.example.mrs_tango.customer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.UserModel
import com.example.mrs_tango.core.data.repository.UserRepository
import com.example.mrs_tango.core.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: UserModel) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class ProfileUpdateState {
    object Idle : ProfileUpdateState()
    object Loading : ProfileUpdateState()
    data class Success(val user: UserModel, val message: String) : ProfileUpdateState()
    data class Error(val message: String) : ProfileUpdateState()
}

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val updateState: StateFlow<ProfileUpdateState> = _updateState.asStateFlow()

    // Mengambil data profil pengguna berdasarkan ID.
    fun fetchProfile(userId: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            when (val res = userRepository.getUserById(userId)) {
                is Result.Success -> _profileState.value = ProfileState.Success(res.data)
                is Result.Error -> _profileState.value = ProfileState.Error(res.message)
                else -> {}
            }
        }
    }

    // Memperbarui data profil pengguna dan mengelola status hasil pembaruan.
    fun updateProfile(
        userId: String,
        name: String,
        phone: String?,
        newAvatarBytes: ByteArray?,
        oldAvatarUrl: String?
    ) {
        viewModelScope.launch {
            _updateState.value = ProfileUpdateState.Loading
            when (val res = userRepository.updateUserProfile(
                userId = userId,
                name = name,
                phone = phone,
                newAvatarBytes = newAvatarBytes,
                oldAvatarUrl = oldAvatarUrl
            )) {
                is Result.Success -> {
                    _profileState.value = ProfileState.Success(res.data)
                    _updateState.value = ProfileUpdateState.Success(res.data, "Profil berhasil diperbarui")
                }
                is Result.Error -> {
                    _updateState.value = ProfileUpdateState.Error(res.message)
                }

                else -> {}
            }
        }
    }

    // Mengatur ulang status pembaruan profil ke kondisi awal.
    fun resetUpdateState() {
        _updateState.value = ProfileUpdateState.Idle
    }
}