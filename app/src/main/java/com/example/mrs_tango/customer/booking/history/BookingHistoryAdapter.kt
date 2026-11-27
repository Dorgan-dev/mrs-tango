package com.example.mrs_tango.customer.booking.history

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mrs_tango.core.data.model.BookingDetailsModel
import com.example.mrs_tango.core.utils.toRupiah
import com.example.mrs_tango.databinding.ItemBookingHistoryBinding

class BookingHistoryAdapter(
    private val onViewProofClick: (String) -> Unit,
    private val onReuploadProofClick: (BookingDetailsModel) -> Unit,
    private val onCancelClick: (BookingDetailsModel) -> Unit
) : ListAdapter<BookingDetailsModel, BookingHistoryAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(
        private val binding: ItemBookingHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Mengisi seluruh informasi booking dan mengatur tampilan berdasarkan status pesanan.
        fun bind(item: BookingDetailsModel) {
            val currentStatus = item.status?.lowercase() ?: "pending"

            binding.tvDeviceName.text = buildString {
                append(item.device?.name ?: "Perangkat PS")
                item.device?.psType?.takeIf { it.isNotBlank() }?.let {
                    append(" ($it)")
                }
            }

            binding.tvBookingSchedule.text =
                "${item.bookingDate ?: "-"} • ${item.formattedTimeRange}"

            binding.tvEstimatedCost.text = item.estimatedCost?.let {
                "Estimasi Total: ${it.toRupiah()}"
            } ?: "Estimasi Total: Open Bill"

            binding.tvDpAmount.text = "DP: ${(item.dpAmount ?: 0).toRupiah()}"

            bindStatus(currentStatus)
            bindProof(item)
            bindCancelReason(item)
            bindRefund(item)
            bindActions(item, currentStatus)
        }


        // Mengatur label dan warna badge sesuai status booking.
        private fun bindStatus(status: String) {
            val (label, background, text) = when (status) {
                "pending" -> Triple("MENUNGGU VERIFIKASI", "#FFF8E1", "#F57F17")
                "confirmed" -> Triple("DIKONFIRMASI", "#E8F5E9", "#1B5E20")
                "completed" -> Triple("SELESAI", "#E3F2FD", "#1565C0")
                "rejected" -> Triple("DITOLAK", "#FFEBEE", "#C62828")
                "cancelled", "canceled" -> Triple("DIBATALKAN", "#FFEBEE", "#C62828")
                "expired" -> Triple("KEDALUWARSA", "#ECEFF1", "#455A64")
                else -> Triple(status.uppercase(), "#ECEFF1", "#455A64")
            }

            binding.tvBookingStatus.apply {
                this.text = label
                backgroundTintList = ColorStateList.valueOf(Color.parseColor(background))
                setTextColor(Color.parseColor(text))
            }
        }

        // Menampilkan tombol untuk melihat bukti pembayaran jika URL bukti DP tersedia.
        private fun bindProof(item: BookingDetailsModel) {
            val proofUrl = item.effectiveDpProof

            binding.btnViewDpProof.apply {
                visibility = if (proofUrl.isNullOrBlank()) View.GONE else View.VISIBLE
                setOnClickListener {
                    proofUrl?.let(onViewProofClick)
                }
            }
        }

        // Menampilkan alasan pembatalan atau penolakan beserta pihak yang melakukannya.
        private fun bindCancelReason(item: BookingDetailsModel) {
            val reason = item.cancelReason

            binding.layoutCancelReason.visibility =
                if (reason.isNullOrBlank()) View.GONE else View.VISIBLE

            if (!reason.isNullOrBlank()) {
                val actor = when {
                    item.cancelledBy.isNullOrBlank() -> ""
                    item.cancelledBy == item.customerId -> "Pelanggan"
                    else -> "Kasir/Outlet"
                }

                val prefix = if (actor.isNotEmpty()) {
                    "Keterangan ($actor): "
                } else {
                    "Keterangan: "
                }

                binding.tvCancelReason.text = "$prefix$reason"
            }
        }

        // Menampilkan informasi pengembalian dana jika data refund tersedia.
        private fun bindRefund(item: BookingDetailsModel) {
            val refund = item.latestRefund

            binding.layoutRefundInfo.visibility =
                if (refund == null) View.GONE else View.VISIBLE

            refund?.let {
                binding.tvRefundDetail.text =
                    "Dana dikembalikan: ${it.refundAmount.toRupiah()} via ${it.refundMethod.uppercase()}\n" +
                            "Catatan: ${it.reason}"
            }
        }

        // Menampilkan tombol upload ulang bukti DP dan pembatalan hanya untuk booking pending.
        private fun bindActions(item: BookingDetailsModel, status: String) {
            val isPending = status == "pending"

            binding.layoutPendingActions.visibility =
                if (isPending) View.VISIBLE else View.GONE

            binding.btnReuploadDp.setOnClickListener {
                onReuploadProofClick(item)
            }

            binding.btnCancelBooking.setOnClickListener {
                onCancelClick(item)
            }
        }
    }

    // Membuat ViewHolder menggunakan layout item riwayat booking.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookingHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // Mengikat data booking ke ViewHolder berdasarkan posisi item.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<BookingDetailsModel>() {
            override fun areItemsTheSame(
                oldItem: BookingDetailsModel,
                newItem: BookingDetailsModel
            ) = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: BookingDetailsModel,
                newItem: BookingDetailsModel
            ) = oldItem == newItem
        }
    }
}