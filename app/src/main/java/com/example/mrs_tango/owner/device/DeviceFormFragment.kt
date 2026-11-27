package com.example.mrs_tango.owner.device

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.mrs_tango.R
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.core.data.model.DeviceModel
import com.example.mrs_tango.core.data.model.DeviceRateModel
import com.example.mrs_tango.core.data.repository.DeviceRepository
import com.example.mrs_tango.core.utils.ImageCompressor
import com.example.mrs_tango.core.utils.Result
import com.example.mrs_tango.databinding.DialogRateFormBinding
import com.example.mrs_tango.databinding.FragmentDeviceFormBinding
import com.example.mrs_tango.databinding.ItemDeviceRateFormBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DeviceFormFragment(
    private val existingDevice: DeviceDetailsModel? = null,
    private val deviceRepository: DeviceRepository = DeviceRepository(),
    private val onSaved: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentDeviceFormBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null

    // Menyimpan daftar shift sementara di form sebelum disimpan ke Supabase
    private val draftRates = mutableListOf<DeviceRateModel>()
    // Menyimpan ID shift lama yang dihapus owner saat mode Edit
    private val deletedRateIds = mutableListOf<String>()

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivDevicePreview.load(uri) { crossfade(true) }
            binding.tvPickImageHint.text = "Ganti foto"
        }
    }

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
        val statusOptions = listOf("available", "in_use", "maintenance")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions)
        binding.actvStatus.setAdapter(statusAdapter)

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

            // Muat semua shift tarif aktif milik device ini
            draftRates.clear()
            draftRates.addAll(existingDevice.rates.filter { it.isActive })

            existingDevice.primaryImageUrl?.let { url ->
                binding.ivDevicePreview.load(url) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background)
                }
                binding.tvPickImageHint.text = "Ganti foto"
            }
        } else {
            binding.tvFormTitle.text = "Tambah Perangkat Baru"
            binding.btnSaveDevice.text = "Simpan Perangkat"
        }

        renderRateList()
    }

    private fun setupListeners() {
        binding.cardSelectImage.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnAddRate.setOnClickListener {
            showRateDialog(existingRate = null, editIndex = null)
        }

        binding.btnSaveDevice.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) {
                binding.etName.error = "Nama unit tidak boleh kosong"
                return@setOnClickListener
            }

            if (draftRates.isEmpty()) {
                Toast.makeText(requireContext(), "Tambahkan minimal 1 tarif perangkat", Toast.LENGTH_SHORT).show()
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

            saveAllData(deviceData)
        }
    }

    private fun renderRateList() {
        binding.llRatesContainer.removeAllViews()
        binding.tvEmptyRates.isVisible = draftRates.isEmpty()

        draftRates.forEachIndexed { index, rate ->
            val itemBinding = ItemDeviceRateFormBinding.inflate(
                layoutInflater,
                binding.llRatesContainer,
                false
            )

            itemBinding.tvRateName.text = rate.rateName
            itemBinding.tvRateTime.text = rate.timeRangeFormatted
            itemBinding.tvRatePrice.text = "${formatRupiah(rate.pricePerHour)}/jam"

            // Ketuk kartu untuk edit shift
            itemBinding.root.setOnClickListener {
                showRateDialog(existingRate = rate, editIndex = index)
            }

            // Ketuk tombol hapus
            itemBinding.btnDeleteRate.setOnClickListener {
                rate.id?.let { deletedRateIds.add(it) }
                draftRates.removeAt(index)
                renderRateList()
            }

            binding.llRatesContainer.addView(itemBinding.root)
        }
    }

    private fun showRateDialog(existingRate: DeviceRateModel?, editIndex: Int?) {
        val dialogBinding = DialogRateFormBinding.inflate(layoutInflater)

        // Format tersimpan untuk database: "HH:mm:00"
        var selectedStartTime: String? = existingRate?.effectiveFrom
        var selectedEndTime: String? = existingRate?.effectiveUntil

        existingRate?.let {
            dialogBinding.etRateName.setText(it.rateName)
            dialogBinding.etStartTime.setText(it.effectiveFrom?.take(5) ?: "")
            dialogBinding.etEndTime.setText(it.effectiveUntil?.take(5) ?: "")
            dialogBinding.etRatePrice.setText(it.pricePerHour.toString())
        }

        dialogBinding.etStartTime.setOnClickListener {
            openTimePicker("Pilih Jam Mulai", selectedStartTime) { display, dbTime ->
                selectedStartTime = dbTime
                dialogBinding.etStartTime.setText(display)
            }
        }

        dialogBinding.etEndTime.setOnClickListener {
            openTimePicker("Pilih Jam Selesai", selectedEndTime) { display, dbTime ->
                selectedEndTime = dbTime
                dialogBinding.etEndTime.setText(display)
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existingRate == null) "Tambah Shift Tarif" else "Edit Shift Tarif")
            .setView(dialogBinding.root)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Simpan", null)
            .create()

        dialog.show()

        // Override tombol positif agar dialog tidak tertutup jika input belum valid
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val rateName = dialogBinding.etRateName.text.toString().trim()
            val price = dialogBinding.etRatePrice.text.toString().toIntOrNull() ?: 0

            if (rateName.isEmpty()) {
                dialogBinding.etRateName.error = "Nama shift wajib diisi"
                return@setOnClickListener
            }

            if ((selectedStartTime == null) != (selectedEndTime == null)) {
                Toast.makeText(
                    requireContext(),
                    "Isi kedua jam mulai & selesai, atau kosongkan keduanya untuk tarif 24 jam",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (price <= 0) {
                dialogBinding.etRatePrice.error = "Masukkan harga per jam yang valid"
                return@setOnClickListener
            }

            val updatedRate = DeviceRateModel(
                id = existingRate?.id,
                deviceId = existingDevice?.id ?: "",
                rateName = rateName,
                pricePerHour = price,
                effectiveFrom = selectedStartTime,
                effectiveUntil = selectedEndTime,
                isActive = true
            )

            if (editIndex != null) {
                draftRates[editIndex] = updatedRate
            } else {
                draftRates.add(updatedRate)
            }

            renderRateList()
            dialog.dismiss()
        }
    }

    private fun openTimePicker(
        title: String,
        currentDbTime: String?,
        onPicked: (displayTime: String, dbTime: String) -> Unit
    ) {
        val parts = currentDbTime?.split(":")
        val defaultHour = parts?.getOrNull(0)?.toIntOrNull() ?: 8
        val defaultMinute = parts?.getOrNull(1)?.toIntOrNull() ?: 0

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(defaultHour)
            .setMinute(defaultMinute)
            .setTitleText(title)
            .build()

        picker.addOnPositiveButtonClickListener {
            val display = String.format(Locale.US, "%02d:%02d", picker.hour, picker.minute)
            val dbTime = "$display:00"
            onPicked(display, dbTime)
        }

        picker.show(parentFragmentManager, "RATE_TIME_PICKER")
    }

    private fun saveAllData(deviceData: DeviceModel) {
        binding.btnSaveDevice.isEnabled = false
        binding.btnSaveDevice.text = "Menyimpan..."

        lifecycleScope.launch {
            // 1. Insert atau Update tabel devices
            val deviceResult = if (existingDevice == null) {
                deviceRepository.addDevice(deviceData)
            } else {
                deviceRepository.updateDevice(existingDevice.id, deviceData)
            }

            if (deviceResult is Result.Error) {
                showError(deviceResult.message)
                return@launch
            }

            val savedDeviceId = (deviceResult as Result.Success).data.id
                ?: existingDevice?.id

            if (savedDeviceId.isNullOrEmpty()) {
                showError("ID perangkat tidak ditemukan")
                return@launch
            }

            // 2. Hapus shift tarif lama yang dibuang owner
            for (rateId in deletedRateIds.toList()) {
                val delResult = deviceRepository.deleteDeviceRate(rateId)
                if (delResult is Result.Error) {
                    showError("Gagal menghapus tarif lama: ${delResult.message}")
                    return@launch
                }
                deletedRateIds.remove(rateId)
            }

            // 3. Simpan shift baru & perbarui shift yang diubah
            for (i in draftRates.indices) {
                val rate = draftRates[i].copy(deviceId = savedDeviceId)
                if (rate.id == null) {
                    val addResult = deviceRepository.addDeviceRate(rate)
                    if (addResult is Result.Error) {
                        showError("Gagal menyimpan tarif '${rate.rateName}': ${addResult.message}")
                        return@launch
                    }
                    // Simpan ID hasil insert ke draftRates agar tidak duplikat jika retry
                    draftRates[i] = (addResult as Result.Success).data
                } else {
                    val updateResult = deviceRepository.updateDeviceRate(rate.id, rate)
                    if (updateResult is Result.Error) {
                        showError("Gagal memperbarui tarif '${rate.rateName}': ${updateResult.message}")
                        return@launch
                    }
                }
            }

            // 4. Upload foto baru jika dipilih
            selectedImageUri?.let { uri ->
                val bytes = ImageCompressor.compressUri(requireContext(), uri)
                if (bytes != null) {
                    val imgResult = deviceRepository.addDeviceImage(
                        deviceId = savedDeviceId,
                        imageBytes = bytes,
                        isPrimary = true
                    )
                    if (imgResult is Result.Error) {
                        showError("Perangkat & tarif tersimpan, tapi foto gagal diunggah: ${imgResult.message}")
                        onSaved()
                        dismiss()
                        return@launch
                    }
                }
            }

            if (isAdded) {
                Toast.makeText(requireContext(), "Perangkat berhasil disimpan", Toast.LENGTH_SHORT).show()
                onSaved()
                dismiss()
            }
        }
    }

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    private fun showError(message: String) {
        if (!isAdded) return
        binding.btnSaveDevice.isEnabled = true
        binding.btnSaveDevice.text = if (existingDevice == null) "Simpan Perangkat" else "Simpan Perubahan"
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}