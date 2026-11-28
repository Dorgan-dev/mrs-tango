package com.example.mrs_tango.customer.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.TransactionWithDetails
import com.example.mrs_tango.core.data.repository.TransactionRepository
import com.example.mrs_tango.core.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TransactionState {
    object Loading : TransactionState()
    data class Success(val transactions: List<TransactionWithDetails>) : TransactionState()
    data class Error(val message: String) : TransactionState()
}

class TransactionHistoryViewModel(
    private val transactionRepository: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val state: StateFlow<TransactionState> = _state.asStateFlow()

    fun fetchTransactions(customerId: String) {
        viewModelScope.launch {
            _state.value = TransactionState.Loading
            when (val res = transactionRepository.getCustomerTransactions(customerId)) {
                is Result.Success -> _state.value = TransactionState.Success(res.data)
                is Result.Error -> _state.value = TransactionState.Error(res.message)
                else -> {}
            }
        }
    }
}