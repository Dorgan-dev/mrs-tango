package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.FnbCategoryModel
import com.example.mrs_tango.core.data.model.FnbItemModel
import com.example.mrs_tango.core.data.model.FnbItemWithCategory
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OwnerFnbRepository {

    private val client get() = SupabaseClient.client
    private val categoriesTable = "fnb_categories"
    private val itemsTable = "fnb_items"
    private val itemWithCategoryColumns = Columns.raw("*, fnb_categories(name)")

    // Mengambil seluruh kategori FnB terurut berdasarkan nama secara menaik.
    suspend fun getAllCategories(): Result<List<FnbCategoryModel>> =
        ioResult("Gagal memuat kategori FnB") {
            client.from(categoriesTable)
                .select { order(column = "name", order = Order.ASCENDING) }
                .decodeList()
        }

    // Menambahkan kategori FnB baru dengan status aktif dan mengembalikan data hasil penyimpanan.
    suspend fun createCategory(name: String): Result<FnbCategoryModel> =
        ioResult("Gagal menambahkan kategori") {
            client.from(categoriesTable)
                .insert(FnbCategoryModel(name = name, isActive = true)) { select() }
                .decodeSingle()
        }

    // Memperbarui nama dan status aktif kategori FnB berdasarkan ID kategori.
    suspend fun updateCategory(categoryId: String, name: String, isActive: Boolean): Result<Boolean> =
        ioResult("Gagal memperbarui kategori") {
            client.from(categoriesTable).update(
                mapOf(
                    "name" to name,
                    "is_active" to isActive
                )
            ) {
                filter { eq("id", categoryId) }
            }
            true
        }

    // Menghapus kategori FnB berdasarkan ID dan mengembalikan pesan jelas jika masih dipakai item menu.
    suspend fun deleteCategory(categoryId: String): Result<Boolean> =
        ioResult(
            defaultError = "Gagal menghapus kategori",
            errorMapper = {
                mapForeignKeyError(
                    e = it,
                    foreignKeyMessage = "Kategori tidak bisa dihapus karena masih memiliki item menu. Nonaktifkan saja statusnya.",
                    fallbackMessage = "Gagal menghapus kategori"
                )
            }
        ) {
            deleteById(categoriesTable, categoryId)
            true
        }

    // Mengambil seluruh item menu FnB beserta nama kategorinya secara berurutan menurut nama menu.
    suspend fun getAllItems(): Result<List<FnbItemWithCategory>> =
        ioResult("Gagal memuat daftar menu FnB") {
            client.from(itemsTable)
                .select(columns = itemWithCategoryColumns) { order(column = "name", order = Order.ASCENDING) }
                .decodeList()
        }

    // Menambahkan item menu FnB baru ke database dan mengembalikan data menu yang tersimpan.
    suspend fun createItem(item: FnbItemModel): Result<FnbItemModel> =
        ioResult("Gagal menambahkan menu") {
            client.from(itemsTable)
                .insert(item) { select() }
                .decodeSingle()
        }

    // Memperbarui informasi kategori, nama, harga, stok, dan ketersediaan item menu berdasarkan ID.
    suspend fun updateItem(itemId: String, item: FnbItemModel): Result<Boolean> =
        ioResult("Gagal memperbarui menu") {
            client.from(itemsTable).update(
                mapOf(
                    "category_id" to item.categoryId,
                    "name" to item.name,
                    "price" to item.price,
                    "stock" to item.stock,
                    "is_available" to item.isAvailable
                )
            ) {
                filter { eq("id", itemId) }
            }
            true
        }

    // Memperbarui stok item menu secara aman dan menyesuaikan status tersedia bila stok habis.
    suspend fun updateStockAndAvailability(
        itemId: String,
        newStock: Int,
        isAvailable: Boolean
    ): Result<Boolean> =
        ioResult("Gagal memperbarui stok menu") {
            val safeStock = newStock.coerceAtLeast(0)
            client.from(itemsTable).update(
                mapOf(
                    "stock" to safeStock,
                    "is_available" to (isAvailable && safeStock > 0)
                )
            ) {
                filter { eq("id", itemId) }
            }
            true
        }

    // Menghapus item menu FnB berdasarkan ID dan mengembalikan pesan informatif jika sudah ada di transaksi.
    suspend fun deleteItem(itemId: String): Result<Boolean> =
        ioResult(
            defaultError = "Gagal menghapus menu",
            errorMapper = {
                mapForeignKeyError(
                    e = it,
                    foreignKeyMessage = "Menu sudah pernah terjual di transaksi. Matikan tombol 'Tersedia' agar tidak tampil di kasir.",
                    fallbackMessage = "Gagal menghapus menu"
                )
            }
        ) {
            deleteById(itemsTable, itemId)
            true
        }

    // Menghapus satu baris data pada tabel tertentu berdasarkan kolom id.
    private suspend fun deleteById(table: String, id: String) {
        client.from(table).delete { filter { eq("id", id) } }
    }

    // Memetakan error foreign key PostgreSQL (kode 23503) menjadi pesan yang mudah dipahami pengguna.
    private fun mapForeignKeyError(
        e: Exception,
        foreignKeyMessage: String,
        fallbackMessage: String
    ): String {
        val msg = e.message.orEmpty()
        return if (msg.contains("23503") || msg.contains("foreign key", ignoreCase = true)) {
            foreignKeyMessage
        } else {
            msg.ifBlank { fallbackMessage }
        }
    }

    // Menjalankan blok suspend di Dispatchers.IO dan mengembalikan hasilnya dalam bentuk Result.
    private suspend fun <T> ioResult(
        defaultError: String,
        errorMapper: ((Exception) -> String)? = null,
        block: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.Success(block())
        } catch (e: Exception) {
            Result.Error(errorMapper?.invoke(e) ?: e.message ?: defaultError)
        }
    }
}