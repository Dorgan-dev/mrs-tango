package com.example.mrs_tango.customer.booking.history

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mrs_tango.core.data.model.BookingModel
import com.example.mrs_tango.databinding.CustomerItemBookingHistoryBinding
import java.text.NumberFormat
import java.util.Locale

class BookingHistoryAdapter(
    private val onCancelClick: (BookingModel) -> Unit
) : RecyclerView.Adapter<BookingHistoryAdapter.HistoryViewHolder>() {

    private var bookingList: List<BookingModel> = emptyList()
    private var deviceNameMap: Map<String, String> = emptyMap()

    fun submitData(newList: List<BookingModel>, devicesMap: Map<String, String>) {
        bookingList = newList
        deviceNameMap = devicesMap
        notifyDataSetChanged()
    }

    fun updateDeviceMap(devicesMap: Map<String, String>) {
        deviceNameMap = devicesMap
        notifyDataSetChanged()
    }

    inner class HistoryViewHolder(private val binding: CustomerItemBookingHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: BookingModel) {
            // Nama Perangkat
            val deviceName = deviceNameMap[booking.deviceId] ?: "Unit #${booking.deviceId.take(6)}"
            binding.tvDeviceName.text = deviceName

            // Tanggal & Jam
            val start = booking.startTime.take(5)
            val end = booking.endTime?.take(5)
            val timeRange = if (end != null) "$start - $end" else "$start (Open Bill)"
            binding.tvBookingDateTime.text = "${booking.usageDate} • $timeRange"

            // Tipe & Durasi
            val typeLabel = if (booking.timeType == "open_bill") "Open Bill" else "Per Jam"
            val durationText = booking.durationMinutes?.let { mins ->
                val hours = mins / 60
                if (hours > 0) " • $hours Jam" else " • $mins Menit"
            } ?: ""
            binding.tvBookingTypeDuration.text = "Tipe: $typeLabel$durationText"

            // Estimasi Biaya
            binding.tvEstimatedCost.text = if (booking.estimatedCost != null) {
                "Estimasi: ${formatRupiah(booking.estimatedCost)}"
            } else {
                "Estimasi: Dihitung saat selesai"
            }

            // Badge Status & Warna
            val status = (booking.status ?: "pending").lowercase()
            binding.tvBookingStatus.text = status.replaceFirstChar { it.uppercase() }

            when (status) {
                "pending" -> {
                    binding.tvBookingStatus.setTextColor(Color.parseColor("#E65100"))
                    binding.tvBookingStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
                }
                "confirmed" -> {
                    binding.tvBookingStatus.setTextColor(Color.parseColor("#1B5E20"))
                    binding.tvBookingStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                }
                "completed" -> {
                    binding.tvBookingStatus.setTextColor(Color.parseColor("#0D47A1"))
                    binding.tvBookingStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E3F2FD"))
                }
                "canceled" -> {
                    binding.tvBookingStatus.setTextColor(Color.parseColor("#B71C1C"))
                    binding.tvBookingStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFEBEE"))
                }
                else -> {
                    binding.tvBookingStatus.setTextColor(Color.parseColor("#424242"))
                    binding.tvBookingStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EEEEEE"))
                }
            }

            // Tampilkan alasan batal jika status canceled
            if (status == "canceled" && !booking.cancelReason.isNullOrBlank()) {
                binding.tvCancelReason.visibility = View.VISIBLE
                binding.tvCancelReason.text = "Alasan batal: ${booking.cancelReason}"
            } else {
                binding.tvCancelReason.visibility = View.GONE
            }

            // Tombol Batalkan hanya aktif jika masih pending
            if (status == "pending") {
                binding.btnCancelBooking.visibility = View.VISIBLE
                binding.btnCancelBooking.setOnClickListener { onCancelClick(booking) }
            } else {
                binding.btnCancelBooking.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = CustomerItemBookingHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(bookingList[position])
    }

    override fun getItemCount(): Int = bookingList.size

    private fun formatRupiah(amount: Int): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }
}