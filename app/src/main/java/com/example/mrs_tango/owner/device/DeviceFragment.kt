package com.example.mrs_tango.owner.device

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.databinding.FragmentDeviceBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class DeviceFragment : Fragment() {

    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OwnerDeviceViewModel by viewModels()
    private lateinit var adapter: OwnerDeviceAdapter
    private var currentOwnerId: String? = null

    // Menginflasi layout fragment perangkat dan mengembalikan root view untuk ditampilkan.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Mengambil ID owner tersimpan, menyiapkan daftar dan listener, lalu memuat data perangkat.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentOwnerId = requireContext()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .getString("user_id", null)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
        viewModel.fetchDevices(currentOwnerId)
    }

    // Menginisialisasi adapter perangkat dengan callback galeri, tarif, edit, dan hapus serta memasangnya ke RecyclerView.
    private fun setupRecyclerView() {
        adapter = OwnerDeviceAdapter(
            onPhotosClick = { device ->
                DeviceGalleryFragment(
                    device = device,
                    onUploadPhoto = { bytes, isPrimary, sortOrder ->
                        viewModel.uploadPhoto(device.id, bytes, isPrimary, sortOrder)
                    },
                    onSetPrimary = { imageId -> viewModel.setPrimaryPhoto(device.id, imageId) },
                    onDeletePhoto = { imageId, storagePath -> viewModel.deletePhoto(imageId, storagePath) }
                ).show(parentFragmentManager, "DeviceGalleryBottomSheet")
            },
            onRatesClick = { device ->
                DeviceRateFragment(
                    device = device,
                    onAddRate = viewModel::addRate,
                    onToggleRate = viewModel::toggleRate,
                    onDeleteRate = viewModel::deleteRate
                ).show(parentFragmentManager, "DeviceRateBottomSheet")
            },
            onEditClick = { device -> openDeviceForm(existingDevice = device) },
            onDeleteClick = ::confirmDeleteDevice
        )

        binding.rvDevices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@DeviceFragment.adapter
        }
    }

    // Mendaftarkan listener aksi pada komponen UI utama seperti tombol tambah perangkat.
    private fun setupListeners() {
        binding.fabAddDevice.setOnClickListener {
            openDeviceForm(existingDevice = null)
        }
    }

    // Mengamati perubahan devicesState dan actionState secara aman sesuai siklus hidup view fragment.
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeListState() }
                launch { observeActionState() }
            }
        }
    }

    // Memperbarui tampilan daftar perangkat, loading, atau pesan error berdasarkan OwnerDeviceListState.
    private suspend fun observeListState() {
        viewModel.devicesState.collect { state ->
            binding.progressBar.isVisible = state is OwnerDeviceListState.Loading
            when (state) {
                is OwnerDeviceListState.Success -> adapter.submitList(state.devices)
                is OwnerDeviceListState.Error -> showToast(state.message, isLong = true)
                is OwnerDeviceListState.Loading -> Unit
            }
        }
    }

    // Menangani status aksi CRUD perangkat dan mereset actionState setelah pesan tampil.
    private suspend fun observeActionState() {
        viewModel.actionState.collect { state ->
            binding.progressBar.isVisible = state is OwnerDeviceActionState.Loading
            when (state) {
                is OwnerDeviceActionState.Success -> {
                    showToast(state.message)
                    viewModel.resetActionState()
                }
                is OwnerDeviceActionState.Error -> {
                    showToast(state.message, isLong = true)
                    viewModel.resetActionState()
                }
                is OwnerDeviceActionState.Idle,
                is OwnerDeviceActionState.Loading -> Unit
            }
        }
    }

    // Membuka bottom sheet form tambah atau edit perangkat dan memuat ulang daftar saat tersimpan.
    private fun openDeviceForm(existingDevice: DeviceDetailsModel?) {
        DeviceFormFragment(existingDevice = existingDevice) {
            viewModel.fetchDevices(currentOwnerId)
        }.show(parentFragmentManager, "DeviceFormBottomSheet")
    }

    // Menampilkan dialog konfirmasi sebelum menghapus perangkat beserta foto dan tarif terkait.
    private fun confirmDeleteDevice(device: DeviceDetailsModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus ${device.name}?")
            .setMessage("Seluruh foto dan tarif milik unit ini akan ikut terhapus.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ -> viewModel.deleteDevice(device) }
            .show()
    }

    // Menampilkan pesan Toast singkat atau panjang dengan konteks fragment yang aman.
    private fun showToast(message: String, isLong: Boolean = false) {
        val duration = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(requireContext(), message, duration).show()
    }

    // Membersihkan referensi view binding saat view fragment dihancurkan untuk mencegah memory leak.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}