package com.example.mrs_tango.owner.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.mrs_tango.core.data.model.DeviceModel
import com.example.mrs_tango.databinding.FragmentDeviceFormBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DeviceFormFragment(
    private val existingDevice: DeviceModel? = null,
    private val onSave: (DeviceModel) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentDeviceFormBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        populateExistingData()
        setupListeners()
    }

    private fun setupDropdowns() {
        // Pilihan Status
        val statusOptions = listOf("available", "in_use", "maintenance")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions)
        binding.actvStatus.setAdapter(statusAdapter)

        // Saran Tipe Konsol (tetap bisa diketik manual karena inputType="text")
        val consoleOptions = listOf("PS5 Disc", "PS5 Digital", "PS4 Pro", "PS4 Slim", "Nintendo Switch", "Xbox Series X")
        val consoleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, consoleOptions)
        binding.actvConsoleType.setAdapter(consoleAdapter)
    }

    private fun populateExistingData() {
        if (existingDevice != null) {
            binding.tvFormTitle.text = "Edit Perangkat"
            binding.btnSaveDevice.text = "Simpan Perubahan"

            binding.etName.setText(existingDevice.name)
            binding.actvStatus.setText(existingDevice.status ?: "available", false)
            binding.actvConsoleType.setText(existingDevice.consoleType, false)
            binding.etConsoleSn.setText(existingDevice.consoleSn)
            binding.etTvName.setText(existingDevice.tvName)
            binding.etTvSn.setText(existingDevice.tvSn)
            binding.etTvIp.setText(existingDevice.tvIpAddress)
            binding.etTvMac.setText(existingDevice.tvMacAddress)
        } else {
            binding.tvFormTitle.text = "Tambah Perangkat Baru"
            binding.btnSaveDevice.text = "Simpan Perangkat"
        }
    }

    private fun setupListeners() {
        binding.btnSaveDevice.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) {
                binding.etName.error = "Nama unit tidak boleh kosong"
                return@setOnClickListener
            }

            val deviceData = DeviceModel(
                id = existingDevice?.id,
                name = name,
                status = binding.actvStatus.text.toString().trim().ifEmpty { "available" },
                consoleType = binding.actvConsoleType.text.toString().trim().ifEmpty { null },
                consoleSn = binding.etConsoleSn.text.toString().trim().ifEmpty { null },
                tvName = binding.etTvName.text.toString().trim().ifEmpty { null },
                tvSn = binding.etTvSn.text.toString().trim().ifEmpty { null },
                tvIpAddress = binding.etTvIp.text.toString().trim().ifEmpty { null },
                tvMacAddress = binding.etTvMac.text.toString().trim().ifEmpty { null }
            )

            onSave(deviceData)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}