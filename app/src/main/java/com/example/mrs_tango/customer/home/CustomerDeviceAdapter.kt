package com.example.mrs_tango.customer.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mrs_tango.R
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.databinding.ConsoleCardBinding

class CustomerDeviceAdapter(
    private val deviceList: List<DeviceDetailsModel>,
    private val onItemClick: (DeviceDetailsModel) -> Unit
) : RecyclerView.Adapter<CustomerDeviceAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ConsoleCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: DeviceDetailsModel) {
            binding.tvConsoleName.text = device.displayName
            binding.tvConsolePrice.text = device.formattedPrice

            binding.imgConsole.load(device.primaryImageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
                fallback(R.drawable.ic_launcher_background)
            }

            binding.root.setOnClickListener {
                onItemClick(device)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ConsoleCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(deviceList[position])
    }

    override fun getItemCount(): Int = deviceList.size
}