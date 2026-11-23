package com.example.mrs_tango.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.UserModel
import com.example.mrs_tango.core.data.repository.AuthRepository
import com.example.mrs_tango.core.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel : ViewModel() {

    // Inisialisasi Repository
    private val authRepository = AuthRepository()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun registerCustomer(
        name: String,
        email: String,
        gender: String,
        birthDate: String,
        password: String,
        confirmPassword: String
    ) {
        // 1. Validasi Input Dasar
        if (name.isBlank() || email.isBlank() || gender.isBlank() || birthDate.isBlank() || password.isBlank()) {
            _registerState.value = RegisterState.Error("Semua kolom harus diisi")
            return
        }

        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("Konfirmasi password tidak cocok")
            return
        }

        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Password minimal 6 karakter")
            return
        }

        // 2. Ubah status menjadi Loading
        _registerState.value = RegisterState.Loading

        // 3. Eksekusi ke Database via Repository
        viewModelScope.launch {
            // Siapkan data yang akan di-insert
            val newUser = UserModel(
                name = name,
                email = email,
                password = password,
                gender = gender,
                birthDate = birthDate
            )

            val result = authRepository.registerCustomer(newUser)

            // 4. Tangani balikan (Result)
            when (result) {
                is Result.Success -> {
                    _registerState.value = RegisterState.Success
                }
                is Result.Error -> {
                    // Karena di Repository kita mengirimkan String ("Email sudah terdaftar", dsb)
                    // Maka kita ambil pesan errornya menggunakan nama properti yang sesuai di class Result.Error milikmu
                    // Asumsi: propertinya bernama 'message' atau 'error'
                    // Ganti result.message dengan nama properti yang ada di dalam data class Result.Error milikmu
                    _registerState.value = RegisterState.Error(result.message)
                }
                is Result.Loading -> {
                    // Jika kamu tidak melakukan apa-apa saat state dari Result adalah Loading,
                    // biarkan kosong atau tambahkan komentar.
                    // State Loading untuk UI sudah kita set di awal fungsi (RegisterState.Loading)
                }
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}