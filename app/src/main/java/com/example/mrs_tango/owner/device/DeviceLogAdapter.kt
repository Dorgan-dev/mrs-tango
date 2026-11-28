package com.example.mrs_tango.owner.device

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mrs_tango.core.data.model.DeviceLogModel
import com.example.mrs_tango.databinding.ItemDeviceLogBinding

class DeviceLogAdapter :
    ListAdapter<DeviceLogModel, DeviceLogAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemDeviceLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Mengisi seluruh elemen tampilan item riwayat log perangkat berdasarkan data DeviceLogModel.
        fun bind(log: DeviceLogModel) = with(binding) {
            tvLogDeviceName.text = log.device?.name ?: "Perangkat PS"
            tvLogTimestamp.text = log.changedAt?.replace("T", " ")?.take(16) ?: "-"
            tvLogTransition.text = "${log.fromStatus.uppercase()} → ${log.toStatus.uppercase()}"

            val hasNote = !log.note.isNullOrBlank()
            tvLogNote.isVisible = hasNote
            if (hasNote) tvLogNote.text = "Catatan: ${log.note}"

            tvLogActor.text = "Oleh: ${log.actor?.name ?: "Sistem"}"
        }
    }

    // Membuat instance ViewHolder baru dengan menginflasi layout item_device_log.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // Mengikat data item pada posisi tertentu ke ViewHolder yang sedang tampil.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DeviceLogModel>() {
            override fun areItemsTheSame(oldItem: DeviceLogModel, newItem: DeviceLogModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: DeviceLogModel, newItem: DeviceLogModel) =
                oldItem == newItem
        }
    }
}