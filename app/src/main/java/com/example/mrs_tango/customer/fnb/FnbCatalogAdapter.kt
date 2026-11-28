package com.example.mrs_tango.customer.fnb

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mrs_tango.core.data.model.FnbItemModel
import com.example.mrs_tango.core.utils.toRupiah
import com.example.mrs_tango.databinding.ItemFnbCatalogBinding

class FnbCatalogAdapter :
    ListAdapter<FnbItemModel, FnbCatalogAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemFnbCatalogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Menampilkan nama, harga, serta status dan warna badge stok pada item menu FnB.
        fun bind(item: FnbItemModel) {
            binding.tvFnbName.text = item.name
            binding.tvFnbPrice.text = item.price.toRupiah()

            val ready = item.isAvailable && item.stock > 0
            if (ready) {
                binding.root.alpha = 1.0f
                binding.tvFnbStockBadge.text = "Tersedia (${item.stock})"
                binding.tvFnbStockBadge.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                binding.tvFnbStockBadge.setTextColor(Color.parseColor("#1B5E20"))
            } else {
                binding.root.alpha = 0.6f
                binding.tvFnbStockBadge.text = "Habis"
                binding.tvFnbStockBadge.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#FFEBEE"))
                binding.tvFnbStockBadge.setTextColor(Color.parseColor("#C62828"))
            }
        }
    }

    // Membuat instance ViewHolder baru dengan meng-inflate layout XML item katalog FnB.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFnbCatalogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    // Mengikat data item menu pada posisi tertentu ke dalam ViewHolder yang ditampilkan.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<FnbItemModel>() {
            // Memeriksa apakah dua item menu mewakili entitas yang sama berdasarkan ID-nya.
            override fun areItemsTheSame(oldItem: FnbItemModel, newItem: FnbItemModel): Boolean =
                oldItem.id == newItem.id

            // Memeriksa apakah seluruh properti item menu masih sama untuk menentukan pembaruan tampilan.
            override fun areContentsTheSame(oldItem: FnbItemModel, newItem: FnbItemModel): Boolean =
                oldItem == newItem
        }
    }
}