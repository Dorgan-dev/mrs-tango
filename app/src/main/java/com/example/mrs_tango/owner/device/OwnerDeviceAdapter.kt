package com.example.mrs_tango.owner.device

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mrs_tango.R
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.core.utils.toRupiah
import com.example.mrs_tango.databinding.OwnerItemDeviceBinding

class OwnerDeviceAdapter(
    private val onPhotosClick: (DeviceDetailsModel) -> Unit,
    private val onRatesClick: (DeviceDetailsModel) -> Unit,
    private val onEditClick: (DeviceDetailsModel) -> Unit,
    private val onDeleteClick: (DeviceDetailsModel) -> Unit
) : ListAdapter<DeviceDetailsModel, OwnerDeviceAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
        private val binding: OwnerItemDeviceBinding,
        private val onPhotosClick: (DeviceDetailsModel) -> Unit,
        private val onRatesClick: (DeviceDetailsModel) -> Unit,
        private val onEditClick: (DeviceDetailsModel) -> Unit,
        private val onDeleteClick: (DeviceDetailsModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // Mengikat seluruh informasi perangkat, tarif aktif, foto thumbnail, status, dan tombol aksi ke item view.
        fun bind(device: DeviceDetailsModel) = with(binding) {
            deviceName.text = device.name.ifBlank { "-" }
            consoleInfo.text = "Konsol: ${device.consoleType ?: "-"} (SN: ${device.consoleSn ?: "-"})"
            tvInfo.text = "TV: ${device.tvName ?: "-"} • IP: ${device.tvIpAddress ?: "-"}"

            val activeRates = device.rates.orEmpty().filter { it.isActive }
            deviceRatesSummary.text = if (activeRates.isEmpty()) {
                "Belum ada tarif aktif"
            } else {
                activeRates.joinToString(" | ") { "${it.rateName}: ${it.pricePerHour.toRupiah()}" }
            }

            ivDeviceThumb.load(device.primaryImageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
            }

            val (bgHex, textHex) = when (device.status?.lowercase()) {
                "available" -> "#E8F5E9" to "#1B5E20"
                "in_use", "booked" -> "#E3F2FD" to "#1565C0"
                "maintenance" -> "#FFEBEE" to "#C62828"
                else -> "#ECEFF1" to "#455A64"
            }
            statusBadge.text = device.status?.uppercase() ?: "UNKNOWN"
            statusBadge.backgroundTintList = ColorStateList.valueOf(bgHex.toColorInt())
            statusBadge.setTextColor(textHex.toColorInt())

            btnManagePhotos.text = "Foto (${device.images.orEmpty().size})"
            btnManageRates.text = "Tarif (${device.rates.orEmpty().size})"

            btnManagePhotos.setOnClickListener { onPhotosClick(device) }
            btnManageRates.setOnClickListener { onRatesClick(device) }
            btnEditDevice.setOnClickListener { onEditClick(device) }
            btnDeleteDevice.setOnClickListener { onDeleteClick(device) }
        }
    }

    // Membuat instance ViewHolder baru dengan menginflasi layout owner_item_device dan meneruskan callback aksi.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OwnerItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onPhotosClick, onRatesClick, onEditClick, onDeleteClick)
    }

    // Mengikat data perangkat pada posisi tertentu ke ViewHolder yang sedang tampil.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DeviceDetailsModel>() {
            override fun areItemsTheSame(oldItem: DeviceDetailsModel, newItem: DeviceDetailsModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: DeviceDetailsModel, newItem: DeviceDetailsModel) =
                oldItem == newItem
        }
    }
}