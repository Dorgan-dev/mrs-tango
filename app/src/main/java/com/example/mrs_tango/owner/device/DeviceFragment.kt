package com.example.mrs_tango.owner.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mrs_tango.core.data.adapter.DeviceAdapter
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.databinding.FragmentDeviceBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class DeviceFragment : Fragment() {

    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DeviceViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(
            onEditClick = { device ->
                showDeviceForm(device)
            },
            onDeleteClick = { device ->
                showDeleteConfirmation(device)
            }
        )

        binding.rvDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }
    }

    private fun setupListeners() {
        // Tombol Tambah Unit (Create)
        binding.fabAddDevice.setOnClickListener {
            showDeviceForm(null)
        }

        // Pencarian Real-time
        binding.inputSearchDevice.addTextChangedListener { text ->
            viewModel.searchDevices(text?.toString() ?: "")
        }
    }

    private fun showDeviceForm(device: DeviceDetailsModel?) {
        val bottomSheet = DeviceFormFragment(
            existingDevice = device,
            onSaved = {
                // Refresh daftar perangkat + foto + tarif setelah berhasil simpan
                viewModel.fetchDevices()
            }
        )
        bottomSheet.show(parentFragmentManager, "DeviceFormBottomSheet")
    }

    private fun showDeleteConfirmation(device: DeviceDetailsModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Perangkat")
            .setMessage("Apakah Anda yakin ingin menghapus \"${device.name}\"? Data yang dihapus tidak dapat dikembalikan.")
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Hapus") { dialog, _ ->
                viewModel.deleteDevice(device.id)
                dialog.dismiss()
            }
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. Amati Daftar Perangkat
                launch {
                    viewModel.listState.collect { state ->
                        when (state) {
                            is DeviceListState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.tvEmptyState.visibility = View.GONE
                            }
                            is DeviceListState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                deviceAdapter.submitList(state.devices)

                                if (state.devices.isEmpty()) {
                                    binding.tvEmptyState.visibility = View.VISIBLE
                                    binding.rvDevices.visibility = View.GONE
                                } else {
                                    binding.tvEmptyState.visibility = View.GONE
                                    binding.rvDevices.visibility = View.VISIBLE
                                }
                            }
                            is DeviceListState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                // 2. Amati Hasil Aksi (Hapus perangkat, dll.)
                launch {
                    viewModel.actionState.collect { action ->
                        when (action) {
                            is DeviceActionState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            is DeviceActionState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), action.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetActionState()
                            }
                            is DeviceActionState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Gagal")
                                    .setMessage(action.message)
                                    .setPositiveButton("OK", null)
                                    .show()
                                viewModel.resetActionState()
                            }
                            is DeviceActionState.Idle -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}