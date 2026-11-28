package com.example.mrs_tango.customer.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mrs_tango.core.data.model.TransactionWithDetails
import com.example.mrs_tango.core.utils.toRupiah
import com.example.mrs_tango.databinding.ItemTransactionHistoryBinding

class TransactionHistoryAdapter(
    private val onItemClick: (TransactionWithDetails) -> Unit
) : ListAdapter<TransactionWithDetails, TransactionHistoryAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemTransactionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Mengisi komponen tampilan item dengan data transaksi dan memasang aksi klik pada baris item.
        fun bind(item: TransactionWithDetails) {
            binding.tvInvoiceNumber.text = item.invoiceNumber
            binding.tvPaymentMethod.text = "${item.paymentMethod.uppercase()} • ${item.status.uppercase()}"

            val unitName = item.session?.device?.name ?: "Unit PS"
            val mins = item.session?.durationMinutes ?: 0
            binding.tvSessionDevice.text = "$unitName • Durasi $mins menit"
            binding.tvPaidDate.text = item.paidAt.replace("T", " ").take(16)
            binding.tvGrandTotal.text = "Total: ${item.grandTotal.toRupiah()}"

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    // Membuat instance ViewHolder baru dengan meng-inflate layout XML item riwayat transaksi.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    // Menghubungkan data transaksi pada posisi tertentu ke dalam ViewHolder yang ditampilkan.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<TransactionWithDetails>() {
            // Memeriksa apakah dua item mewakili transaksi yang sama berdasarkan ID uniknya.
            override fun areItemsTheSame(
                oldItem: TransactionWithDetails,
                newItem: TransactionWithDetails
            ): Boolean = oldItem.id == newItem.id

            // Memeriksa apakah seluruh isi data dua transaksi masih identik untuk menentukan perlu tidaknya pembaruan UI.
            override fun areContentsTheSame(
                oldItem: TransactionWithDetails,
                newItem: TransactionWithDetails
            ): Boolean = oldItem == newItem
        }
    }
}