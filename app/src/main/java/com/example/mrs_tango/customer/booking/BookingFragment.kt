package com.example.mrs_tango.customer.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mrs_tango.core.data.model.BookingModel
import com.example.mrs_tango.databinding.FragmentBookingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingFragment : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookingViewModel by viewModels()

    private var deviceId: String = ""
    private var deviceName: String = ""

    // Gunakan Double secara konsisten
    private var pricePerHour: Double = 15000.0

    private var selectedDate: String = ""
    private var selectedStartHour: Int = -1
    private var selectedStartMinute: Int = -1
    private var selectedStartTime: String = ""

    private var calculatedEndTime: String? = null
    private var calculatedDurationMinutes: Int? = null
    private var calculatedEstimatedCost: Int? = null

    companion object {
        private const val ARG_DEVICE_ID = "arg_device_id"
        private const val ARG_DEVICE_NAME = "arg_device_name"
        private const val ARG_PRICE_PER_HOUR = "arg_price_per_hour"

        fun newInstance(
            deviceId: String,
            deviceName: String,
            pricePerHour: Double
        ): BookingFragment {
            return BookingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DEVICE_ID, deviceId)
                    putString(ARG_DEVICE_NAME, deviceName)
                    putDouble(ARG_PRICE_PER_HOUR, pricePerHour)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            deviceId = it.getString(ARG_DEVICE_ID, "")
            deviceName = it.getString(ARG_DEVICE_NAME, "Perangkat")
            pricePerHour = it.getDouble(ARG_PRICE_PER_HOUR, 15000.0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        setupTimeTypeDropdown()
        setupListeners()
        observeViewModel()
        recalculateSummary()
    }

    private fun setupHeader() {
        binding.tvSelectedDeviceName.text = deviceName

        binding.tvDeviceRate.text =
            "Tarif: ${formatRupiah(pricePerHour)} / jam"
    }

    private fun setupTimeTypeDropdown() {

        val options = listOf("hourly", "open_bill")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            options
        )

        binding.actvTimeType.setAdapter(adapter)

        // Default booking
        binding.actvTimeType.setText("hourly", false)

        binding.actvTimeType.setOnItemClickListener { _, _, position, _ ->

            val selectedType = options[position]

            binding.tilDuration.visibility =
                if (selectedType == "open_bill") {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            recalculateSummary()
        }
    }

    private fun setupListeners() {

        binding.etUsageDate.setOnClickListener {
            showDatePicker()
        }

        binding.etStartTime.setOnClickListener {
            showTimePicker()
        }

        binding.etDurationHours.addTextChangedListener {
            recalculateSummary()
        }

        binding.btnSubmitBooking.setOnClickListener {
            submitBooking()
        }
    }

    private fun showDatePicker() {

        val calendar = Calendar.getInstance()

        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->

                selectedDate = String.format(
                    Locale.US,
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    dayOfMonth
                )

                binding.etUsageDate.setText(selectedDate)

                recalculateSummary()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.datePicker.minDate = System.currentTimeMillis() - 1000

        dialog.show()
    }

    private fun showTimePicker() {

        val calendar = Calendar.getInstance()

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->

                selectedStartHour = hourOfDay
                selectedStartMinute = minute

                selectedStartTime = String.format(
                    Locale.US,
                    "%02d:%02d:00",
                    hourOfDay,
                    minute
                )

                binding.etStartTime.setText(
                    String.format(
                        Locale.US,
                        "%02d:%02d",
                        hourOfDay,
                        minute
                    )
                )

                recalculateSummary()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun recalculateSummary() {

        val timeType = binding.actvTimeType.text.toString()

        // Open Bill
        if (timeType == "open_bill") {

            calculatedEndTime = null
            calculatedDurationMinutes = null
            calculatedEstimatedCost = null

            binding.tvEndTimeInfo.text =
                "Perkiraan Selesai: Open Bill (Fleksibel)"

            binding.tvEstimatedCost.text =
                "Estimasi Biaya: Dihitung saat selesai"

            return
        }

        val hours = binding.etDurationHours.text
            .toString()
            .toIntOrNull() ?: 0

        if (hours <= 0) {

            calculatedEndTime = null
            calculatedDurationMinutes = null
            calculatedEstimatedCost = 0

            binding.tvEndTimeInfo.text =
                "Perkiraan Selesai: -"

            binding.tvEstimatedCost.text =
                "Estimasi Biaya: ${formatRupiah(0.0)}"

            return
        }

        // Hitung durasi
        val durationMinutes = hours * 60

        calculatedDurationMinutes = durationMinutes

        // Hitung biaya
        val estimatedCost = hours * pricePerHour

        calculatedEstimatedCost = estimatedCost.toInt()

        binding.tvEstimatedCost.text =
            "Estimasi Biaya: ${formatRupiah(estimatedCost)}"

        // Hitung waktu selesai
        if (
            selectedStartHour != -1 &&
            selectedStartMinute != -1
        ) {

            val calendar = Calendar.getInstance().apply {

                set(Calendar.HOUR_OF_DAY, selectedStartHour)
                set(Calendar.MINUTE, selectedStartMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                add(Calendar.MINUTE, durationMinutes)
            }

            val endHour = calendar.get(Calendar.HOUR_OF_DAY)
            val endMinute = calendar.get(Calendar.MINUTE)

            calculatedEndTime = String.format(
                Locale.US,
                "%02d:%02d:00",
                endHour,
                endMinute
            )

            val endDate = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            ).format(calendar.time)

            val nextDay = endDate != selectedDate

            binding.tvEndTimeInfo.text =
                if (nextDay) {
                    String.format(
                        Locale.US,
                        "Perkiraan Selesai: %02d:%02d (Hari berikutnya)",
                        endHour,
                        endMinute
                    )
                } else {
                    String.format(
                        Locale.US,
                        "Perkiraan Selesai: %02d:%02d",
                        endHour,
                        endMinute
                    )
                }

        } else {

            calculatedEndTime = null

            binding.tvEndTimeInfo.text =
                "Perkiraan Selesai: Pilih jam mulai dulu"
        }
    }

    private fun submitBooking() {

        val prefs = requireContext().getSharedPreferences(
            "user_pref",
            Context.MODE_PRIVATE
        )

        val customerId = prefs.getString("user_id", null)

        if (customerId.isNullOrEmpty()) {
            showAlert(
                "Sesi Kosong",
                "user_id tidak ditemukan. Silakan login ulang."
            )
            return
        }

        if (deviceId.isEmpty()) {
            showAlert(
                "Device ID Kosong",
                "ID perangkat belum dikirim ke halaman booking."
            )
            return
        }

        if (selectedDate.isEmpty() || selectedStartTime.isEmpty()) {
            showAlert(
                "Data Belum Lengkap",
                "Pilih tanggal dan jam mulai terlebih dahulu."
            )
            return
        }

        // Validasi tanggal dan jam tidak boleh di masa lalu
        val selectedCalendar = Calendar.getInstance().apply {

            set(
                selectedDate.substring(0, 4).toInt(),
                selectedDate.substring(5, 7).toInt() - 1,
                selectedDate.substring(8, 10).toInt(),
                selectedStartHour,
                selectedStartMinute,
                0
            )
        }

        if (selectedCalendar.timeInMillis < System.currentTimeMillis()) {
            showAlert(
                "Waktu Tidak Valid",
                "Tanggal dan jam booking tidak boleh di masa lalu."
            )
            return
        }

        val timeType = binding.actvTimeType.text
            .toString()
            .ifEmpty { "hourly" }

        if (timeType !in listOf("hourly", "open_bill")) {
            showAlert(
                "Jenis Booking Tidak Valid",
                "Silakan pilih jenis booking terlebih dahulu."
            )
            return
        }

        if (
            timeType == "hourly" &&
            (calculatedDurationMinutes == null ||
                    calculatedDurationMinutes!! <= 0)
        ) {

            binding.etDurationHours.error =
                "Masukkan durasi minimal 1 jam"

            return
        }

        if (timeType == "hourly" && calculatedEndTime == null) {
            showAlert(
                "Data Belum Lengkap",
                "Silakan pilih jam mulai dan durasi booking."
            )
            return
        }

        val newBooking = BookingModel(
            deviceId = deviceId,
            customerId = customerId,
            usageDate = selectedDate,
            startTime = selectedStartTime,
            timeType = timeType,
            endTime = calculatedEndTime,
            durationMinutes = calculatedDurationMinutes,
            estimatedCost = calculatedEstimatedCost,
            status = "pending"
        )

        viewModel.createBooking(newBooking)
    }

    private fun showAlert(
        title: String,
        message: String
    ) {

        if (!isAdded) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun observeViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.actionState.collect { state ->

                    when (state) {

                        is BookingActionState.Loading -> {

                            binding.progressBar.visibility =
                                View.VISIBLE

                            binding.btnSubmitBooking.isEnabled =
                                false
                        }

                        is BookingActionState.Success -> {

                            binding.progressBar.visibility =
                                View.GONE

                            binding.btnSubmitBooking.isEnabled =
                                true

                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()

                            viewModel.resetActionState()

                            parentFragmentManager.popBackStack()
                        }

                        is BookingActionState.Error -> {

                            binding.progressBar.visibility =
                                View.GONE

                            binding.btnSubmitBooking.isEnabled =
                                true

                            showAlert(
                                "Booking Gagal",
                                state.message
                            )

                            viewModel.resetActionState()
                        }

                        is BookingActionState.Idle -> {
                            // Tidak ada tindakan
                        }
                    }
                }
            }
        }
    }

    private fun formatRupiah(amount: Double): String {

        val formatter = NumberFormat.getCurrencyInstance(
            Locale("in", "ID")
        )

        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0

        return formatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
