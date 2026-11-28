package com.example.mrs_tango.customer.fnb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.FnbCategoryModel
import com.example.mrs_tango.core.data.model.FnbItemModel
import com.example.mrs_tango.core.data.repository.FnbRepository
import com.example.mrs_tango.core.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FnbCatalogState {
    object Loading : FnbCatalogState()
    data class Success(
        val categories: List<FnbCategoryModel>,
        val filteredItems: List<FnbItemModel>,
        val selectedCategoryId: String?
    ) : FnbCatalogState()
    data class Error(val message: String) : FnbCatalogState()
}

class FnbCatalogViewModel(
    private val fnbRepository: FnbRepository = FnbRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<FnbCatalogState>(FnbCatalogState.Loading)
    val state: StateFlow<FnbCatalogState> = _state.asStateFlow()

    private var cachedCategories: List<FnbCategoryModel> = emptyList()
    private var cachedItems: List<FnbItemModel> = emptyList()
    private var currentCategoryId: String? = null

    // Memuat kategori dan item menu FnB dari repository lalu memperbarui state tampilan katalog.
    fun loadCatalog() {
        viewModelScope.launch {
            _state.value = FnbCatalogState.Loading

            val catRes = fnbRepository.getActiveCategories()
            val itemRes = fnbRepository.getFnbItems()

            if (itemRes is Result.Error) {
                _state.value = FnbCatalogState.Error(itemRes.message)
                return@launch
            }

            cachedCategories = if (catRes is Result.Success) catRes.data else emptyList()
            cachedItems = (itemRes as Result.Success).data
            selectCategory(currentCategoryId)
        }
    }

    // Menyaring daftar item menu berdasarkan kategori terpilih dan memancarkan state sukses terbaru.
    fun selectCategory(categoryId: String?) {
        currentCategoryId = categoryId
        val filtered = if (categoryId == null) {
            cachedItems
        } else {
            cachedItems.filter { it.categoryId == categoryId }
        }
        _state.value = FnbCatalogState.Success(
            categories = cachedCategories,
            filteredItems = filtered,
            selectedCategoryId = currentCategoryId
        )
    }
}