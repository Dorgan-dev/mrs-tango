package com.example.mrs_tango.owner.device

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.core.data.model.DeviceRateModel
import com.example.mrs_tango.core.utils.toRupiah
import com.example.mrs_tango.databinding.FragmentDeviceRateBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale

class DeviceRateFragment(
    private val device: DeviceDetailsModel,
    private val onAddRate: (DeviceRateModel) -> Unit,
    private val onToggleRate: (rateId: String, active: Boolean) -> Unit,
    private val onDeleteRate: (rateId: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentDeviceRateBinding? = null
    private val binding get() = _binding!!

    private var startTimeSql: String? = null
    private var endTimeSql: String? = null

    // Menginflasi layout bottom sheet dan mengembalikan root view untuk fragment.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceRateBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Menginisialisasi judul sheet, daftar tarif yang ada, pemilih waktu shift, dan aksi tambah tarif.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSheetTitle.text = "Shift Tarif — ${device.name}"
        renderExistingRates()

        binding.btnPickStartTime.setOnClickListener {
            showTimePicker("Jam Mulai Shift") { hour, minute ->
                startTimeSql = formatTime(hour, minute, withSeconds = true)
                binding.btnPickStartTime.text = "Mulai: ${formatTime(hour, minute)}"
            }
        }

        binding.btnPickEndTime.setOnClickListener {
            showTimePicker("Jam Selesai Shift") { hour, minute ->
                endTimeSql = formatTime(hour, minute, withSeconds = true)
                binding.btnPickEndTime.text = "Selesai: ${formatTime(hour, minute)}"
            }
        }

        binding.btnAddRate.setOnClickListener { submitNewRate() }
    }

    // Memvalidasi input harga dan mengirimkan model tarif baru sebelum menutup bottom sheet.
    private fun submitNewRate() {
        val price = binding.etRatePrice.text.toString().toIntOrNull() ?: 0
        if (price <= 0) {
            Toast.makeText(requireContext(), "Masukkan harga per jam yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        val rateName = binding.etRateName.text.toString().trim().ifEmpty { "Reguler" }
        onAddRate(
            DeviceRateModel(
                deviceId = device.id,
                rateName = rateName,
                pricePerHour = price,
                effectiveFrom = startTimeSql,
                effectiveUntil = endTimeSql,
                isActive = true
            )
        )
        dismiss()
    }

    // Menampilkan seluruh daftar tarif perangkat atau pesan kosong jika belum ada tarif.
    @SuppressLint("SetTextI18n")
    private fun renderExistingRates() = with(binding.containerRatesList) {
        removeAllViews()
        val rates = device.rates.orEmpty()

        if (rates.isEmpty()) {
            addView(
                TextView(requireContext()).apply {
                    text = "Belum ada shift tarif untuk unit ini."
                    setPadding(0, 8, 0, 16)
                }
            )
            return
        }

        rates.forEach { rate -> addView(buildRateRow(rate)) }
    }

    // Membangun satu baris tampilan tarif beserta switch status aktif dan tombol hapus.
    @SuppressLint("SetTextI18n")
    private fun buildRateRow(rate: DeviceRateModel): View {
        val ctx = requireContext()
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 12, 0, 12)

            addView(
                TextView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    text = "${rate.rateName} (${rate.timeRangeFormatted})\n${rate.pricePerHour.toRupiah()}/jam"
                }
            )

            addView(
                SwitchMaterial(ctx).apply {
                    isChecked = rate.isActive
                    setOnCheckedChangeListener { buttonView, checked ->
                        if (buttonView.isPressed) {
                            rate.id?.let { onToggleRate(it, checked) }
                        }
                    }
                }
            )

            addView(
                MaterialButton(
                    ContextThemeWrapper(ctx, com.google.android.material.R.style.Widget_MaterialComponents_Button_TextButton),
                    null,
                    0
                ).apply {
                    text = "Hapus"
                    setTextColor("#C62828".toColorInt())
                    setOnClickListener {
                        rate.id?.let { onDeleteRate(it) }
                        dismiss()
                    }
                }
            )
        }
    }

    // Menampilkan dialog MaterialTimePicker 24 jam dan mengembalikan jam serta menit yang dipilih.
    private fun showTimePicker(title: String, onPicked: (hour: Int, minute: Int) -> Unit) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText(title)
            .build()
        picker.addOnPositiveButtonClickListener { onPicked(picker.hour, picker.minute) }
        picker.show(parentFragmentManager, title)
    }

    // Memformat jam dan menit ke format HH:mm atau HH:mm:ss untuk kebutuhan UI maupun SQL.
    private fun formatTime(hour: Int, minute: Int, withSeconds: Boolean = false): String {
        val pattern = if (withSeconds) "%02d:%02d:00" else "%02d:%02d"
        return String.format(Locale.US, pattern, hour, minute)
    }

    // Membersihkan referensi view binding saat view fragment dihancurkan untuk mencegah memory leak.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}