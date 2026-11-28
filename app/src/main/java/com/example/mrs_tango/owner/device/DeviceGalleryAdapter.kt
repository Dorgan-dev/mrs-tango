package com.example.mrs_tango.owner.device

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mrs_tango.R
import com.example.mrs_tango.core.data.model.DeviceImageModel
import com.example.mrs_tango.databinding.ItemDeviceGalleryPhotoBinding

class DeviceGalleryAdapter(
    private val onSetPrimaryClick: (DeviceImageModel) -> Unit,
    private val onDeleteClick: (DeviceImageModel) -> Unit
) : ListAdapter<DeviceImageModel, DeviceGalleryAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
        private val binding: ItemDeviceGalleryPhotoBinding,
        private val onSetPrimaryClick: (DeviceImageModel) -> Unit,
        private val onDeleteClick: (DeviceImageModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // Mengikat data foto galeri, status foto utama, dan listener tombol aksi ke tampilan item.
        fun bind(photo: DeviceImageModel) = with(binding) {
            ivPhotoItem.load(photo.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
            }

            tvPrimaryBadge.isVisible = photo.isPrimary
            btnSetPrimary.isVisible = !photo.isPrimary
            btnSetPrimary.setOnClickListener { onSetPrimaryClick(photo) }
            btnDeletePhoto.setOnClickListener { onDeleteClick(photo) }
        }
    }

    // Membuat instance ViewHolder baru dengan menginflasi layout item_device_gallery_photo dan meneruskan callback.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceGalleryPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onSetPrimaryClick, onDeleteClick)
    }

    // Mengikat item foto pada posisi tertentu ke ViewHolder yang sedang tampil.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DeviceImageModel>() {
            override fun areItemsTheSame(oldItem: DeviceImageModel, newItem: DeviceImageModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: DeviceImageModel, newItem: DeviceImageModel) =
                oldItem == newItem
        }
    }
}