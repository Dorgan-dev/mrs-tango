package com.example.mrs_tango.customer.booking.history

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mrs_tango.core.data.model.BookingModel
import com.example.mrs_tango.customer.booking.BookingActionState
import com.example.mrs_tango.customer.booking.BookingHistoryState
import com.example.mrs_tango.customer.booking.BookingViewModel
import com.example.mrs_tango.databinding.FragmentBookingHistoryBinding
import com.example.mrs_tango.owner.device.DeviceListState
import com.example.mrs_tango.owner.device.DeviceViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class BookingHistoryFragment : Fragment() {

    private var _binding: FragmentBookingHistoryBinding? = null
    private val binding get() = _binding!!
    private val bookingViewModel: BookingViewModel by viewModels()
    private val deviceViewModel: DeviceViewModel by viewModels()
    private lateinit var historyAdapter: BookingHistoryAdapter
    private var deviceNameMap: Map<String, String> = emptyMap()
    private var currentCustomerId: String = ""

    // Membuat tampilan fragment menggunakan ViewBinding.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Menginisialisasi ID pelanggan, RecyclerView, dan pengamatan perubahan state ViewModel.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        currentCustomerId = prefs.getString("user_id", "") ?: ""

        setupRecyclerView()
        observeViewModels()
    }

    // Memuat daftar perangkat dan riwayat booking pelanggan setiap kali fragment kembali aktif.
    override fun onResume() {
        super.onResume()
        deviceViewModel.fetchDevices()

        if (currentCustomerId.isNotEmpty()) {
            bookingViewModel.fetchCustomerBookings(currentCustomerId)
        } else {
            binding.tvEmptyHistory.apply {
                text = "Sesi login tidak ditemukan"
                visibility = View.VISIBLE
            }
            binding.rvBookingHistory.visibility = View.GONE
        }
    }

    // Mengatur RecyclerView dan menghubungkan adapter dengan aksi pembatalan booking.
    private fun setupRecyclerView() {
        historyAdapter = BookingHistoryAdapter(
            onCancelClick = { booking -> showCancelDialog(booking) }
        )

        binding.rvBookingHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    // Menampilkan dialog konfirmasi pembatalan booking beserta input alasan pembatalan.
    private fun showCancelDialog(booking: BookingModel) {
        val inputReason = EditText(requireContext()).apply {
            hint = "Masukkan alasan pembatalan"
        }

        val padding = (20 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(requireContext()).apply {
            setPadding(padding, padding / 2, padding, 0)
            addView(inputReason)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Batalkan Booking")
            .setMessage("Yakin ingin membatalkan pesanan pada ${booking.usageDate}?")
            .setView(container)
            .setNegativeButton("Tidak", null)
            .setPositiveButton("Ya, Batalkan") { _, _ ->
                val reason = inputReason.text.toString().trim()

                if (reason.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Alasan pembatalan wajib diisi",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    booking.id?.let { id ->
                        bookingViewModel.cancelBooking(id, reason, currentCustomerId)
                    }
                }
            }
            .show()
    }

    // Mengamati perubahan state perangkat, riwayat booking, dan aksi pembatalan selama lifecycle aktif.
    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Mengubah daftar perangkat menjadi peta ID perangkat dan nama perangkat.
                launch {
                    deviceViewModel.listState.collect { state ->
                        if (state is DeviceListState.Success) {
                            deviceNameMap = state.devices.associate { device ->
                                val label = buildString {
                                    append(device.name ?: "Unit")
                                    if (!device.consoleType.isNullOrBlank()) {
                                        append(" (${device.consoleType})")
                                    }
                                }
                                (device.id ?: "") to label
                            }
                            historyAdapter.updateDeviceMap(deviceNameMap)
                        }
                    }
                }

                // Memperbarui tampilan RecyclerView berdasarkan state riwayat booking pelanggan.
                launch {
                    bookingViewModel.historyState.collect { state ->
                        when (state) {
                            is BookingHistoryState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.tvEmptyHistory.visibility = View.GONE
                            }
                            is BookingHistoryState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                historyAdapter.submitData(state.bookings, deviceNameMap)

                                val isEmpty = state.bookings.isEmpty()
                                binding.tvEmptyHistory.visibility =
                                    if (isEmpty) View.VISIBLE else View.GONE
                                binding.rvBookingHistory.visibility =
                                    if (isEmpty) View.GONE else View.VISIBLE
                            }
                            is BookingHistoryState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    state.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }

                // Menampilkan hasil proses pembatalan booking berupa pesan berhasil atau gagal.
                launch {
                    bookingViewModel.actionState.collect { action ->
                        when (action) {
                            is BookingActionState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            is BookingActionState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    action.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                bookingViewModel.resetActionState()
                            }
                            is BookingActionState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Gagal Membatalkan")
                                    .setMessage(action.message)
                                    .setPositiveButton("OK", null)
                                    .show()
                                bookingViewModel.resetActionState()
                            }
                            is BookingActionState.Idle -> Unit
                        }
                    }
                }
            }
        }
    }

    // Membersihkan referensi ViewBinding ketika tampilan fragment dihancurkan.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}