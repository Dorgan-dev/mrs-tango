package com.example.mrs_tango.owner.fnb

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mrs_tango.core.data.model.FnbItemWithCategory
import com.example.mrs_tango.core.utils.toRupiah
import com.example.mrs_tango.databinding.ItemOwnerFnbBinding

class OwnerFnbAdapter(
    private val onStockDelta: (FnbItemWithCategory, Int) -> Unit,
    private val onToggleAvailable: (FnbItemWithCategory, Boolean) -> Unit,
    private val onEditClick: (FnbItemWithCategory) -> Unit,
    private val onDeleteClick: (FnbItemWithCategory) -> Unit
) : ListAdapter<FnbItemWithCategory, OwnerFnbAdapter.ViewHolder>(DiffCallback) {

    // Class ini digunakan untuk mengelola tampilan setiap item F&B dan menangani interaksi pengguna pada daftar menu.
    inner class ViewHolder(private val binding: ItemOwnerFnbBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Method ini digunakan untuk menampilkan informasi menu dan mengatur aksi perubahan stok, ketersediaan, edit, serta hapus menu.
        fun bind(item: FnbItemWithCategory) {
            binding.tvItemCategory.text = (item.category?.name ?: "UMUM").uppercase()
            binding.tvItemName.text = item.name
            binding.tvItemPrice.text = item.price.toRupiah()
            binding.tvItemStock.text = "Stok: ${item.stock}"

            // Hindari trigger listener berulang saat RecyclerView melakukan recycling
            binding.switchAvailable.setOnCheckedChangeListener(null)
            binding.switchAvailable.isChecked = item.isAvailable
            binding.switchAvailable.setOnCheckedChangeListener { _, isChecked ->
                onToggleAvailable(item, isChecked)
            }

            binding.btnMinusStock.setOnClickListener { onStockDelta(item, -1) }
            binding.btnPlusStock.setOnClickListener { onStockDelta(item, +1) }
            binding.btnEditItem.setOnClickListener { onEditClick(item) }
            binding.btnDeleteItem.setOnClickListener { onDeleteClick(item) }
        }
    }

    // Method ini digunakan untuk membuat ViewHolder baru dengan menghubungkan layout item F&B menggunakan View Binding.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOwnerFnbBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    // Method ini digunakan untuk menghubungkan data menu pada posisi tertentu dengan ViewHolder agar ditampilkan pada RecyclerView.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Object ini digunakan untuk membandingkan data menu lama dan baru agar RecyclerView hanya memperbarui item yang mengalami perubahan.
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<FnbItemWithCategory>() {

            // Method ini digunakan untuk memeriksa apakah dua item menu merupakan data yang sama berdasarkan ID.
            override fun areItemsTheSame(oldItem: FnbItemWithCategory, newItem: FnbItemWithCategory) =
                oldItem.id == newItem.id

            // Method ini digunakan untuk memeriksa apakah seluruh isi data dua item menu masih sama atau mengalami perubahan.
            override fun areContentsTheSame(oldItem: FnbItemWithCategory, newItem: FnbItemWithCategory) =
                oldItem == newItem
        }
    }
}