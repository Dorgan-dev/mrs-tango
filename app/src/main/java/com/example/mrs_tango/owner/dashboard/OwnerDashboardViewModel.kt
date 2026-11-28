package com.example.mrs_tango.owner.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.repository.OwnerDashboardRepository
import com.example.mrs_tango.core.data.repository.OwnerDashboardSummary
import com.example.mrs_tango.core.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OwnerDashboardState {
    object Loading : OwnerDashboardState()
    data class Success(val summary: OwnerDashboardSummary) : OwnerDashboardState()
    data class Error(val message: String) : OwnerDashboardState()
}

class OwnerDashboardViewModel(
    private val repository: OwnerDashboardRepository = OwnerDashboardRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<OwnerDashboardState>(OwnerDashboardState.Loading)
    val state: StateFlow<OwnerDashboardState> = _state.asStateFlow()

    fun loadSummary() {
        viewModelScope.launch {
            _state.value = OwnerDashboardState.Loading
            when (val res = repository.getDashboardSummary()) {
                is Result.Success -> _state.value = OwnerDashboardState.Success(res.data)
                is Result.Error -> _state.value = OwnerDashboardState.Error(res.message)
                else -> {}
            }
        }
    }
}