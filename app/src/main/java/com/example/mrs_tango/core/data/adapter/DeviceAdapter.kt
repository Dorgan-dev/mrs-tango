package com.example.mrs_tango.core.data.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mrs_tango.core.data.model.DeviceModel
import com.example.mrs_tango.databinding.OwnerItemDeviceBinding

class DeviceAdapter(
    private val onEditClick: (DeviceModel) -> Unit,
    private val onDeleteClick: (DeviceModel) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private var deviceList: List<DeviceModel> = emptyList()

    fun submitList(newList: List<DeviceModel>) {
        deviceList = newList
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(private val binding: OwnerItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: DeviceModel) {
            binding.deviceName.text = device.name ?: "-"

            // Info Konsol
            val type = device.consoleType ?: "-"
            val sn = device.consoleSn ?: "-"
            binding.consoleInfo.text = "Konsol: $type (SN: $sn)"

            // Info TV & IP
            val tvName = device.tvName ?: "-"
            val ip = device.tvIpAddress ?: "-"
            binding.tvInfo.text = "TV: $tvName • IP: $ip"

            // Badge Status & Pewarnaan Dinamis
            val status = device.status ?: "available"
            binding.deviceStatus.text = status.replaceFirstChar { it.uppercase() }

            when (status.lowercase()) {
                "available" -> {
                    binding.deviceStatus.setTextColor(Color.parseColor("#1B5E20"))
                    binding.deviceStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                }
                "in_use", "dipakai", "booked" -> {
                    binding.deviceStatus.setTextColor(Color.parseColor("#E65100"))
                    binding.deviceStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
                }
                "maintenance", "rusak" -> {
                    binding.deviceStatus.setTextColor(Color.parseColor("#B71C1C"))
                    binding.deviceStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFEBEE"))
                }
                else -> {
                    binding.deviceStatus.setTextColor(Color.parseColor("#424242"))
                    binding.deviceStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EEEEEE"))
                }
            }

            // Listener Tombol Aksi
            binding.btnEdit.setOnClickListener { onEditClick(device) }
            binding.btnDelete.setOnClickListener { onDeleteClick(device) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = OwnerItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(deviceList[position])
    }

    override fun getItemCount(): Int = deviceList.size
}