package com.example.mrs_tango.owner.fnb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrs_tango.core.data.model.FnbCategoryModel
import com.example.mrs_tango.core.data.model.FnbItemModel
import com.example.mrs_tango.core.data.model.FnbItemWithCategory
import com.example.mrs_tango.core.data.repository.OwnerFnbRepository
import com.example.mrs_tango.core.utils.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OwnerFnbDataState {
    object Loading : OwnerFnbDataState()
    data class Success(
        val categories: List<FnbCategoryModel>,
        val filteredItems: List<FnbItemWithCategory>,
        val selectedCategoryId: String?
    ) : OwnerFnbDataState()
    data class Error(val message: String) : OwnerFnbDataState()
}

sealed class OwnerFnbActionState {
    object Idle : OwnerFnbActionState()
    object Loading : OwnerFnbActionState()
    data class Success(val message: String) : OwnerFnbActionState()
    data class Error(val message: String) : OwnerFnbActionState()
}

class OwnerFnbViewModel(
    private val repository: OwnerFnbRepository = OwnerFnbRepository()
) : ViewModel() {

    private val _dataState = MutableStateFlow<OwnerFnbDataState>(OwnerFnbDataState.Loading)
    val dataState: StateFlow<OwnerFnbDataState> = _dataState.asStateFlow()

    private val _actionState = MutableStateFlow<OwnerFnbActionState>(OwnerFnbActionState.Idle)
    val actionState: StateFlow<OwnerFnbActionState> = _actionState.asStateFlow()

    private var categoriesCache: List<FnbCategoryModel> = emptyList()
    private var itemsCache: List<FnbItemWithCategory> = emptyList()
    private var selectedCategoryId: String? = null

    // Memuat kategori dan item menu FnB secara paralel lalu memperbarui dataState sesuai filter aktif.
    fun loadAll() {
        viewModelScope.launch {
            _dataState.value = OwnerFnbDataState.Loading

            val catDeferred = async { repository.getAllCategories() }
            val itemDeferred = async { repository.getAllItems() }
            val catRes = catDeferred.await()
            val itemRes = itemDeferred.await()

            if (itemRes is Result.Error) {
                _dataState.value = OwnerFnbDataState.Error(itemRes.message)
                return@launch
            }

            categoriesCache = (catRes as? Result.Success)?.data.orEmpty()
            itemsCache = (itemRes as Result.Success).data
            filterByCategory(selectedCategoryId)
        }
    }

    // Menyaring daftar menu dari cache berdasarkan ID kategori dan memancarkan OwnerFnbDataState.Success.
    fun filterByCategory(categoryId: String?) {
        selectedCategoryId = categoryId
        val filtered = categoryId?.let { targetId ->
            itemsCache.filter { it.categoryId == targetId }
        } ?: itemsCache

        _dataState.value = OwnerFnbDataState.Success(
            categories = categoriesCache,
            filteredItems = filtered,
            selectedCategoryId = selectedCategoryId
        )
    }

    // Mengembalikan daftar kategori FnB yang tersimpan di cache memori.
    fun getCachedCategories(): List<FnbCategoryModel> = categoriesCache

    // Menambahkan kategori FnB baru lalu memuat ulang daftar kategori dan menu saat berhasil.
    fun addCategory(name: String) {
        runAction("Kategori ditambahkan") {
            repository.createCategory(name)
        }
    }

    // Memperbarui nama serta status aktif kategori FnB lalu menyegarkan data saat berhasil.
    fun updateCategory(categoryId: String, name: String, isActive: Boolean) {
        runAction("Kategori diperbarui") {
            repository.updateCategory(categoryId, name, isActive)
        }
    }

    // Menghapus kategori FnB berdasarkan ID, mereset filter jika terhapus, lalu memuat ulang data.
    fun deleteCategory(categoryId: String) {
        runAction(
            successMessage = "Kategori dihapus",
            onSuccess = { if (selectedCategoryId == categoryId) selectedCategoryId = null }
        ) {
            repository.deleteCategory(categoryId)
        }
    }

    // Menyimpan menu FnB baru atau memperbarui menu yang sudah ada lalu memuat ulang daftar.
    fun saveItem(existingId: String?, item: FnbItemModel) {
        val message = if (existingId == null) "Menu baru ditambahkan" else "Menu diperbarui"
        runAction(message) {
            if (existingId == null) repository.createItem(item) else repository.updateItem(existingId, item)
        }
    }

    // Menyesuaikan jumlah stok menu dengan selisih delta dan menonaktifkan ketersediaan jika stok nol.
    fun adjustStock(item: FnbItemWithCategory, delta: Int) {
        val updatedStock = (item.stock + delta).coerceAtLeast(0)
        val shouldAvailable = updatedStock > 0 && item.isAvailable
        updateStockState(item.id, updatedStock, shouldAvailable)
    }

    // Mengubah status ketersediaan menu FnB tanpa mengubah jumlah stok saat ini.
    fun toggleAvailability(item: FnbItemWithCategory, isAvailable: Boolean) {
        updateStockState(item.id, item.stock, isAvailable)
    }

    // Menghapus item menu FnB berdasarkan ID lalu menyegarkan daftar menu saat berhasil.
    fun deleteItem(itemId: String) {
        runAction("Menu berhasil dihapus") {
            repository.deleteItem(itemId)
        }
    }

    // Mengembalikan actionState ke kondisi Idle setelah pesan status atau error ditangani UI.
    fun resetActionState() {
        _actionState.value = OwnerFnbActionState.Idle
    }

    // Memperbarui stok dan status tersedia pada repository serta menyegarkan daftar jika berhasil.
    private fun updateStockState(itemId: String, stock: Int, isAvailable: Boolean) {
        runAction(successMessage = null, showLoading = false) {
            repository.updateStockAndAvailability(itemId, stock, isAvailable)
        }
    }

    // Menjalankan operasi mutasi repository secara terpusat serta memperbarui actionState dan daftar.
    private fun runAction(
        successMessage: String?,
        showLoading: Boolean = true,
        onSuccess: (() -> Unit)? = null,
        block: suspend () -> Result<Any>
    ) {
        viewModelScope.launch {
            if (showLoading) _actionState.value = OwnerFnbActionState.Loading
            when (val res = block()) {
                is Result.Success -> {
                    onSuccess?.invoke()
                    if (successMessage != null) {
                        _actionState.value = OwnerFnbActionState.Success(successMessage)
                    }
                    loadAll()
                }
                is Result.Error -> _actionState.value = OwnerFnbActionState.Error(res.message)
                else -> Unit
            }
        }
    }
}