package com.example.mrs_tango.owner.device

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.core.data.model.DeviceImageModel
import com.example.mrs_tango.core.utils.ImageCompressor
import com.example.mrs_tango.databinding.FragmentDeviceGalleryBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeviceGalleryFragment(
    private val device: DeviceDetailsModel,
    private val onUploadPhoto: (bytes: ByteArray, isPrimary: Boolean, sortOrder: Int) -> Unit,
    private val onSetPrimary: (imageId: String) -> Unit,
    private val onDeletePhoto: (imageId: String, storagePath: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentDeviceGalleryBinding? = null
    private val binding get() = _binding!!

    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(::handlePickedImage) }

    // Menginflasi layout bottom sheet galeri dan mengembalikan root view untuk ditampilkan.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Menyiapkan judul galeri, daftar foto terurut, serta aksi tombol tambah foto.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGalleryTitle.text = "Galeri Foto — ${device.name}"
        setupGalleryList()

        binding.btnAddPhoto.setOnClickListener {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    // Mengompres gambar terpilih lalu meneruskannya ke callback unggah sebelum menutup sheet.
    private fun handlePickedImage(uri: Uri) {
        val bytes = ImageCompressor.compressUri(
            context = requireContext(),
            uri = uri,
            maxDimension = 1080,
            quality = 75
        )

        if (bytes == null) {
            Toast.makeText(requireContext(), "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
            return
        }

        val existingImages = device.images.orEmpty()
        val nextOrder = (existingImages.maxOfOrNull { it.sortOrder } ?: -1) + 1
        onUploadPhoto(bytes, existingImages.isEmpty(), nextOrder)
        dismiss()
    }

    // Mengurutkan foto perangkat, mengatur status kosong, dan memasang adapter RecyclerView.
    private fun setupGalleryList() = with(binding) {
        val sortedPhotos = device.images.orEmpty().sortedWith(
            compareByDescending<DeviceImageModel> { it.isPrimary }.thenBy { it.sortOrder }
        )

        val isEmpty = sortedPhotos.isEmpty()
        tvEmptyGallery.isVisible = isEmpty
        rvGalleryPhotos.isVisible = !isEmpty

        val adapter = DeviceGalleryAdapter(
            onSetPrimaryClick = { photo ->
                photo.id?.let { id ->
                    onSetPrimary(id)
                    dismiss()
                }
            },
            onDeleteClick = ::confirmDeletePhoto
        )

        rvGalleryPhotos.layoutManager = LinearLayoutManager(requireContext())
        rvGalleryPhotos.adapter = adapter
        adapter.submitList(sortedPhotos)
    }

    // Menampilkan dialog konfirmasi sebelum menghapus foto secara permanen.
    private fun confirmDeletePhoto(photo: DeviceImageModel) {
        val photoId = photo.id ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Foto?")
            .setMessage("Foto ini akan dihapus permanen dari galeri dan penyimpanan.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                onDeletePhoto(photoId, photo.storagePath)
                dismiss()
            }
            .show()
    }

    // Membersihkan referensi view binding saat view fragment dihancurkan untuk mencegah memory leak.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}