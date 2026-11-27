package com.example.mrs_tango.customer.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.mrs_tango.R
import com.example.mrs_tango.core.data.model.BookingModel
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.core.utils.ImageCompressor
import com.example.mrs_tango.core.utils.QrCodeGenerator
import com.example.mrs_tango.core.utils.toRupiah
import com.example.mrs_tango.databinding.FragmentBookingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class BookingFragment : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookingViewModel by viewModels()
    private var deviceId: String = ""
    private var currentDevice: DeviceDetailsModel? = null
    private var selectedDate: String = ""
    private var selectedHour: Int = 12
    private var selectedMinute: Int = 0
    private var selectedDurationHours: Int = 1
    private var selectedTimeType: String = "per_hour" // "per_hour" atau "free_play"
    private var selectedProofUri: Uri? = null
    private var calculatedEstimatedCost: Int? = null
    private var calculatedDpAmount: Int = 0

    private val pickProofLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedProofUri = uri
            binding.ivDpProofPreview.load(uri) { crossfade(true) }
            binding.tvPickDpHint.text = "Ganti foto bukti transfer"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deviceId = arguments?.getString("EXTRA_DEVICE_ID") ?: ""
        if (deviceId.isEmpty()) {
            Toast.makeText(requireContext(), "ID perangkat tidak ditemukan", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        initDefaultDateTime()
        setupListeners()
        observeViewModel()
        viewModel.loadBookingFormData(deviceId)
    }

    private fun initDefaultDateTime() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        selectedDate = dateFormat.format(calendar.time)
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
        selectedMinute = 0

        // Sesuaikan ID etDate & etStartTime jika di fragment_booking.xml berbeda
        binding.etUsageDate.setText(selectedDate)
        binding.etStartTime.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute))
        binding.tilDuration.editText?.setText(selectedDurationHours.toString())
    }

    private fun setupListeners() {
        // 1. Pilih Tanggal Main
        binding.etUsageDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val picker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    binding.etUsageDate.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            picker.datePicker.minDate = System.currentTimeMillis() - 1000
            picker.show()
        }

        // 2. Pilih Jam Mulai (Format 24 Jam)
        binding.etStartTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    selectedHour = hourOfDay
                    selectedMinute = minute
                    binding.etStartTime.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute))
                    recalculatePriceAndDp()
                },
                selectedHour,
                selectedMinute,
                true
            ).show()
        }

        // 3. Ubah Durasi Jam Main
        binding.tilDuration.editText?.doAfterTextChanged { editable ->
            val hours = editable?.toString()?.toIntOrNull()
            selectedDurationHours = if (hours != null && hours > 0) hours else 1
            recalculatePriceAndDp()
        }

        // 4. Pilih Tipe Booking (Per Jam vs Open Bill)
        binding.rgBookingType.setOnCheckedChangeListener { _, checkedId ->
            selectedTimeType = if (checkedId == R.id.rbOpenBill) "free_play" else "per_hour"
            binding.tilDuration.visibility = if (selectedTimeType == "per_hour") View.VISIBLE else View.GONE
            recalculatePriceAndDp()
        }

        // 5. Pilih Foto Bukti Transfer DP
        binding.cardPickDpProof.setOnClickListener {
            pickProofLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // 6. Tombol Konfirmasi Booking
        binding.btnConfirmBooking.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun recalculatePriceAndDp() {
        val device = currentDevice ?: return

        if (selectedTimeType == "per_hour") {
            val total = device.calculateTotalCost(selectedHour, selectedMinute, selectedDurationHours)
            val dp = total / 2

            calculatedEstimatedCost = total
            calculatedDpAmount = dp

            binding.tvEstimatedTotal.text = "Estimasi Total ($selectedDurationHours Jam): ${total.toRupiah()}"
            binding.tvDpRequired.text = "Wajib DP (50%): ${dp.toRupiah()}"
        } else {
            val firstHourRate = device.getRateAt(selectedHour, selectedMinute)?.pricePerHour ?: 0

            calculatedEstimatedCost = null
            calculatedDpAmount = firstHourRate

            binding.tvEstimatedTotal.text = "Estimasi Total: Dihitung saat selesai (Open Bill)"
            binding.tvDpRequired.text = "Wajib DP (Tarif 1 Jam): ${firstHourRate.toRupiah()}"
        }
    }

    private fun validateAndSubmit() {
        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val customerId = prefs.getString("user_id", "") ?: ""

        if (customerId.isEmpty()) {
            Toast.makeText(requireContext(), "Silakan login ulang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(requireContext(), "Pilih tanggal bermain terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedTimeType == "per_hour") {
            val rawDuration = binding.tilDuration.editText?.text?.toString()?.toIntOrNull() ?: 0
            if (rawDuration <= 0) {
                binding.tilDuration.error = "Durasi minimal 1 jam"
                return
            }
            binding.tilDuration.error = null
        }

        val proofUri = selectedProofUri
        if (proofUri == null) {
            Toast.makeText(requireContext(), "Unggah foto bukti transfer DP terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val compressedBytes = ImageCompressor.compressUri(requireContext(), proofUri, maxDimension = 1024, quality = 75)
        if (compressedBytes == null) {
            Toast.makeText(requireContext(), "Gagal memproses gambar bukti transfer", Toast.LENGTH_SHORT).show()
            return
        }

        val startTimeStr = String.format(Locale.US, "%02d:%02d:00", selectedHour, selectedMinute)
        val endTimeStr = if (selectedTimeType == "per_hour") {
            val endHour = (selectedHour + selectedDurationHours) % 24
            String.format(Locale.US, "%02d:%02d:00", endHour, selectedMinute)
        } else {
            null
        }
        val durationMins = if (selectedTimeType == "per_hour") selectedDurationHours * 60 else null

        val expiresCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.MINUTE, 15)
        }
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val booking = BookingModel(
            deviceId = deviceId,
            customerId = customerId,
            usageDate = selectedDate,
            startTime = startTimeStr,
            timeType = selectedTimeType,
            endTime = endTimeStr,
            durationMinutes = durationMins,
            estimatedCost = calculatedEstimatedCost,
            dpAmount = calculatedDpAmount,
            status = "pending",
            expiresAt = isoFormat.format(expiresCalendar.time)
        )
        viewModel.submitBooking(booking, compressedBytes)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.formInitState.collect { state ->
                        when (state) {
                            is BookingFormInitState.Loading -> {
                                binding.btnConfirmBooking.isEnabled = false
                            }
                            is BookingFormInitState.Ready -> {
                                binding.btnConfirmBooking.isEnabled = true
                                currentDevice = state.device
                                binding.tvDeviceTitle.text = state.device.name
                                binding.tvStorePaymentTitle.text = "Scan QRIS — ${state.storeName}"

                                val qrBitmap = QrCodeGenerator.generateBitmap(state.qrisString)
                                if (qrBitmap != null) {
                                    binding.ivQrisCode.setImageBitmap(qrBitmap)
                                }

                                recalculatePriceAndDp()
                            }
                            is BookingFormInitState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.actionState.collect { state ->
                        when (state) {
                            is BookingActionState.Loading -> {
                                binding.btnConfirmBooking.isEnabled = false
                                binding.btnConfirmBooking.text = "Mengunggah & Mengirim..."
                            }
                            is BookingActionState.Success -> {
                                binding.btnConfirmBooking.isEnabled = true
                                binding.btnConfirmBooking.text = "Konfirmasi Booking"
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Pesanan Terkirim")
                                    .setMessage(state.message)
                                    .setCancelable(false)
                                    .setPositiveButton("OK") { _, _ ->
                                        parentFragmentManager.popBackStack()
                                    }
                                    .show()
                                viewModel.resetActionState()
                            }
                            is BookingActionState.Error -> {
                                binding.btnConfirmBooking.isEnabled = true
                                binding.btnConfirmBooking.text = "Konfirmasi Booking"
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                viewModel.resetActionState()
                            }
                            is BookingActionState.Idle -> {}
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(
            deviceId: String,
            deviceName: String? = null,
            pricePerHour: Double? = null
        ): BookingFragment {
            val fragment = BookingFragment()
            val args = Bundle().apply {
                putString("EXTRA_DEVICE_ID", deviceId)
                if (deviceName != null) putString("EXTRA_DEVICE_NAME", deviceName)
                if (pricePerHour != null) putDouble("EXTRA_PRICE_PER_HOUR", pricePerHour)
            }
            fragment.arguments = args
            return fragment
        }
    }
}