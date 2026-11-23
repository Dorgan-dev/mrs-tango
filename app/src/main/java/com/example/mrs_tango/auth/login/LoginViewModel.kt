package com.example.mrs_tango.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.UserModel
import com.example.mrs_tango.core.utils.Result
import com.example.mrs_tango.core.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: UserModel, val targetDestination: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email dan password wajib diisi")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.loginUser(email.trim(), password)

            when (result) {
                is Result.Success -> {
                    val user = result.data // Sesuaikan nama properti data di Result.Success milikmu

                    val destination = when (user.role?.lowercase()) { // Pengecekan 3 Role dalam 1 tabel Gunakan .lowercase()
                        "owner" -> "OWNER_HOME"
                        "cashier" -> "CASHIER_HOME"
                        "customer" -> "CUSTOMER_HOME"
                        else -> "UNKNOWN_ROLE"
                    }

                    if (destination == "UNKNOWN_ROLE") {
                        _loginState.value = LoginState.Error("Role pengguna tidak valid")
                    } else {
                        _loginState.value = LoginState.Success(user, destination)
                    }
                }
                is Result.Error -> {
                    _loginState.value = LoginState.Error(result.message)
                }
                is Result.Loading -> {}
            }
        }
    }
}