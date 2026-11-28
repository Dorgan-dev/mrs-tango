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

class DeviceAdapter(
    private val onPhotosClick: (DeviceDetailsModel) -> Unit,
    private val onRatesClick: (DeviceDetailsModel) -> Unit,
    private val onEditClick: (DeviceDetailsModel) -> Unit,
    private val onDeleteClick: (DeviceDetailsModel) -> Unit
) : ListAdapter<DeviceDetailsModel, DeviceAdapter.DeviceViewHolder>(DiffCallback) {

    class DeviceViewHolder(
        private val binding: OwnerItemDeviceBinding,
        private val onPhotosClick: (DeviceDetailsModel) -> Unit,
        private val onRatesClick: (DeviceDetailsModel) -> Unit,
        private val onEditClick: (DeviceDetailsModel) -> Unit,
        private val onDeleteClick: (DeviceDetailsModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // Mengikat data perangkat, tarif aktif, foto thumbnail, badge status, dan listener tombol aksi ke view item.
        fun bind(device: DeviceDetailsModel) = with(binding) {
            deviceName.text = device.name.ifBlank { "-" }
            consoleInfo.text = "Konsol: ${device.consoleType ?: "-"} (SN: ${device.consoleSn ?: "-"})"
            tvInfo.text = "TV: ${device.tvName ?: "-"} • IP: ${device.tvIpAddress ?: "-"}"

            ivDeviceThumb.load(device.primaryImageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
            }

            val activeRates = device.rates.orEmpty().filter { it.isActive }
            deviceRatesSummary.text = if (activeRates.isEmpty()) {
                "Belum ada tarif aktif"
            } else {
                activeRates.joinToString(" | ") { "${it.rateName}: ${it.pricePerHour.toRupiah()}" }
            }

            val status = device.status ?: "available"
            val (textHex, bgHex) = when (status.lowercase()) {
                "available" -> "#1B5E20" to "#E8F5E9"
                "in_use", "dipakai", "booked" -> "#E65100" to "#FFF3E0"
                "maintenance", "rusak" -> "#B71C1C" to "#FFEBEE"
                else -> "#424242" to "#EEEEEE"
            }

            statusBadge.text = status.replaceFirstChar { it.uppercase() }
            statusBadge.setTextColor(textHex.toColorInt())
            statusBadge.backgroundTintList = ColorStateList.valueOf(bgHex.toColorInt())

            btnManagePhotos.text = "Foto (${device.images.orEmpty().size})"
            btnManageRates.text = "Tarif (${device.rates.orEmpty().size})"

            btnManagePhotos.setOnClickListener { onPhotosClick(device) }
            btnManageRates.setOnClickListener { onRatesClick(device) }
            btnEditDevice.setOnClickListener { onEditClick(device) }
            btnDeleteDevice.setOnClickListener { onDeleteClick(device) }
        }
    }

    // Membuat instance DeviceViewHolder baru dengan menginflasi layout owner_item_device dan meneruskan callback.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = OwnerItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding, onPhotosClick, onRatesClick, onEditClick, onDeleteClick)
    }

    // Mengikat item perangkat pada posisi tertentu ke DeviceViewHolder yang sedang ditampilkan.
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
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