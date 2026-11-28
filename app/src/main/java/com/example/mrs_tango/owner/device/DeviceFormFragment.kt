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
    private val draftRates = mutableListOf<DeviceRateModel>()
    private val deletedRateIds = mutableListOf<String>()

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivDevicePreview.load(uri) { crossfade(true) }
            binding.tvPickImageHint.text = "Ganti foto"
        }
    }

    // Method ini digunakan untuk menginisialisasi tampilan form perangkat menggunakan View Binding.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDeviceFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Method ini digunakan untuk menyiapkan dropdown, mengisi data perangkat yang sudah ada, dan mengatur event listener.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        populateExistingData()
        setupListeners()
    }

    // Method ini digunakan untuk mengatur pilihan status perangkat dan jenis konsol pada dropdown.
    private fun setupDropdowns() {
        binding.actvStatus.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("available", "in_use", "maintenance"))
        )
        binding.actvConsoleType.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                listOf("PS5 Disc", "PS5 Digital", "PS4 Pro", "PS4 Slim", "Nintendo Switch", "Xbox Series X")
            )
        )
    }

    // Method ini digunakan untuk mengisi form dengan data perangkat dan tarif yang sudah tersimpan atau menampilkan form kosong untuk perangkat baru.
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

            draftRates.clear()
            draftRates.addAll(existingDevice.activeRates)

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

    // Method ini digunakan untuk mengatur aksi pemilihan foto, penambahan tarif, serta validasi dan penyimpanan data perangkat.
    private fun setupListeners() {
        binding.cardSelectImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

            saveAllData(
                DeviceModel(
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
            )
        }
    }

    // Method ini digunakan untuk menampilkan daftar tarif perangkat beserta informasi dan tombol edit maupun hapus.
    private fun renderRateList() {
        binding.llRatesContainer.removeAllViews()
        binding.tvEmptyRates.isVisible = draftRates.isEmpty()

        draftRates.forEachIndexed { index, rate ->
            val itemBinding = ItemDeviceRateFormBinding.inflate(layoutInflater, binding.llRatesContainer, false)
            itemBinding.tvRateName.text = rate.rateName
            itemBinding.tvRateTime.text = rate.timeRangeFormatted
            itemBinding.tvRatePrice.text = "${formatRupiah(rate.pricePerHour)}/jam"
            itemBinding.root.setOnClickListener { showRateDialog(existingRate = rate, editIndex = index) }
            itemBinding.btnDeleteRate.setOnClickListener {
                rate.id?.let { deletedRateIds.add(it) }
                draftRates.removeAt(index)
                renderRateList()
            }
            binding.llRatesContainer.addView(itemBinding.root)
        }
    }

    // Method ini digunakan untuk menampilkan dialog tambah atau edit tarif serta memvalidasi data tarif sebelum disimpan ke daftar sementara.
    private fun showRateDialog(existingRate: DeviceRateModel?, editIndex: Int?) {
        val dialogBinding = DialogRateFormBinding.inflate(layoutInflater)
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
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val rateName = dialogBinding.etRateName.text.toString().trim()
            val price = dialogBinding.etRatePrice.text.toString().toIntOrNull() ?: 0

            if (rateName.isEmpty()) {
                dialogBinding.etRateName.error = "Nama shift wajib diisi"
                return@setOnClickListener
            }
            if ((selectedStartTime == null) != (selectedEndTime == null)) {
                Toast.makeText(requireContext(), "Isi kedua jam mulai & selesai, atau kosongkan keduanya untuk tarif 24 jam", Toast.LENGTH_SHORT).show()
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

            if (editIndex != null) draftRates[editIndex] = updatedRate else draftRates.add(updatedRate)
            renderRateList()
            dialog.dismiss()
        }
    }

    // Method ini digunakan untuk menampilkan pemilih waktu dan mengembalikan waktu yang dipilih dalam format tampilan dan format database.
    private fun openTimePicker(title: String, currentDbTime: String?, onPicked: (displayTime: String, dbTime: String) -> Unit) {
        val parts = currentDbTime?.split(":")
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(parts?.getOrNull(0)?.toIntOrNull() ?: 8)
            .setMinute(parts?.getOrNull(1)?.toIntOrNull() ?: 0)
            .setTitleText(title)
            .build()

        picker.addOnPositiveButtonClickListener {
            val display = String.format(Locale.US, "%02d:%02d", picker.hour, picker.minute)
            onPicked(display, "$display:00")
        }
        picker.show(parentFragmentManager, "RATE_TIME_PICKER")
    }

    // Method ini digunakan untuk menyimpan data perangkat, perubahan tarif, serta foto baru secara berurutan ke repository.
    private fun saveAllData(deviceData: DeviceModel) {
        binding.btnSaveDevice.isEnabled = false
        binding.btnSaveDevice.text = "Menyimpan..."

        lifecycleScope.launch {
            val deviceResult = if (existingDevice == null) {
                deviceRepository.addDevice(deviceData)
            } else {
                deviceRepository.updateDevice(existingDevice.id, deviceData)
            }
            if (deviceResult is Result.Error) return@launch showError(deviceResult.message)

            val savedDeviceId = (deviceResult as Result.Success).data.id ?: existingDevice?.id
            if (savedDeviceId.isNullOrEmpty()) return@launch showError("ID perangkat tidak ditemukan")

            for (rateId in deletedRateIds.toList()) {
                val delResult = deviceRepository.deleteDeviceRate(rateId)
                if (delResult is Result.Error) return@launch showError("Gagal menghapus tarif lama: ${delResult.message}")
                deletedRateIds.remove(rateId)
            }

            for (i in draftRates.indices) {
                val rate = draftRates[i].copy(deviceId = savedDeviceId)
                if (rate.id == null) {
                    val addResult = deviceRepository.addDeviceRate(rate)
                    if (addResult is Result.Error) return@launch showError("Gagal menyimpan tarif '${rate.rateName}': ${addResult.message}")
                    draftRates[i] = (addResult as Result.Success).data
                } else {
                    val updateResult = deviceRepository.updateDeviceRate(rate.id, rate)
                    if (updateResult is Result.Error) return@launch showError("Gagal memperbarui tarif '${rate.rateName}': ${updateResult.message}")
                }
            }

            selectedImageUri?.let { uri ->
                val bytes = ImageCompressor.compressUri(requireContext(), uri) ?: return@let
                val imgResult = deviceRepository.addDeviceImage(deviceId = savedDeviceId, imageBytes = bytes, isPrimary = true)
                if (imgResult is Result.Error) {
                    showError("Perangkat & tarif tersimpan, tapi foto gagal diunggah: ${imgResult.message}")
                    onSaved()
                    dismiss()
                    return@launch
                }
            }

            if (isAdded) {
                Toast.makeText(requireContext(), "Perangkat berhasil disimpan", Toast.LENGTH_SHORT).show()
                onSaved()
                dismiss()
            }
        }
    }

    // Method ini digunakan untuk mengubah nominal angka menjadi format mata uang Rupiah tanpa angka desimal.
    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    // Method ini digunakan untuk menampilkan pesan kesalahan dan mengaktifkan kembali tombol simpan ketika proses penyimpanan gagal.
    private fun showError(message: String) {
        if (!isAdded) return
        binding.btnSaveDevice.isEnabled = true
        binding.btnSaveDevice.text = if (existingDevice == null) "Simpan Perangkat" else "Simpan Perubahan"
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    // Method ini digunakan untuk membersihkan View Binding ketika tampilan Bottom Sheet dihancurkan agar mencegah kebocoran memori.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}