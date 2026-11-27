package com.example.mrs_tango.customer.booking.history

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.mrs_tango.core.data.model.BookingDetailsModel
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.ImageCompressor
import com.example.mrs_tango.customer.booking.BookingActionState
import com.example.mrs_tango.databinding.FragmentBookingHistoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class BookingHistoryFragment : Fragment() {

    private var _binding: FragmentBookingHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookingHistoryViewModel by viewModels()
    private lateinit var adapter: BookingHistoryAdapter

    private var currentCustomerId = ""
    private var targetReuploadBooking: BookingDetailsModel? = null

    private val reuploadPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val booking = targetReuploadBooking ?: return@registerForActivityResult

        if (uri != null) {
            val bytes = ImageCompressor.compressUri(
                requireContext(),
                uri,
                maxDimension = 1024,
                quality = 75
            )

            if (bytes != null) {
                viewModel.reuploadDpProof(
                    customerId = currentCustomerId,
                    bookingId = booking.id,
                    newBytes = bytes,
                    oldUrl = booking.dpProofFile
                )
            } else {
                showToast("Gagal memproses gambar")
            }
        }
    }

    // Membuat tampilan riwayat booking menggunakan ViewBinding.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Mengambil ID pelanggan, mengatur RecyclerView, filter, dan observer sebelum memuat riwayat booking.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        currentCustomerId = prefs.getString("user_id", null)
            ?: prefs.getString("id", null)
                    ?: SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: ""

        setupRecyclerView()
        setupFilterChips()
        observeViewModel()

        if (currentCustomerId.isNotEmpty()) {
            viewModel.fetchBookings(currentCustomerId)
        } else {
            binding.progressBar.visibility = View.GONE
            binding.tvEmptyState?.apply {
                text = "Sesi login tidak ditemukan"
                visibility = View.VISIBLE
            }
        }
    }

    // Mengatur RecyclerView dan menghubungkan setiap aksi pada item booking dengan fungsinya.
    private fun setupRecyclerView() {
        adapter = BookingHistoryAdapter(
            onViewProofClick = ::showProofPreviewDialog,
            onReuploadProofClick = { booking ->
                targetReuploadBooking = booking
                reuploadPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onCancelClick = ::showCancelDialog
        )

        binding.rvBookingHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BookingHistoryFragment.adapter
        }
    }

    // Menghubungkan pilihan kategori filter dengan fungsi penyaringan pada ViewModel.
    private fun setupFilterChips() {
        binding.chipAll?.setOnClickListener { viewModel.applyFilter("ALL") }
        binding.chipActive?.setOnClickListener { viewModel.applyFilter("ACTIVE") }
        binding.chipCompleted?.setOnClickListener { viewModel.applyFilter("HISTORY") }
    }

    // Menampilkan bukti transfer DP dalam dialog menggunakan Coil untuk memuat gambar.
    private fun showProofPreviewDialog(imageUrl: String) {
        val imageView = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            minimumHeight = 700
            maxHeight = 1200
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(32, 32, 32, 16)

            load(imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_dialog_alert)
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Bukti Transfer DP")
            .setView(imageView)
            .setPositiveButton("Tutup", null)
            .show()
    }

    // Menampilkan dialog konfirmasi pembatalan booking beserta input alasan pembatalan.
    private fun showCancelDialog(booking: BookingDetailsModel) {
        val inputReason = EditText(requireContext()).apply {
            hint = "Masukkan alasan pembatalan..."
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Batalkan Pesanan?")
            .setMessage(
                "Pesanan ${booking.device?.name ?: "PS"} pada ${booking.bookingDate} akan dibatalkan."
            )
            .setView(inputReason)
            .setNegativeButton("Tidak", null)
            .setPositiveButton("Ya, Batalkan") { _, _ ->
                val reason = inputReason.text.toString().trim()
                    .ifEmpty { "Dibatalkan oleh pelanggan" }

                viewModel.cancelBooking(currentCustomerId, booking.id, reason)
            }
            .show()
    }

    // Mengamati perubahan state riwayat booking dan aksi pelanggan selama lifecycle fragment aktif.
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.historyState.collect { state ->
                        when (state) {
                            is BookingHistoryState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }

                            is BookingHistoryState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                adapter.submitList(state.bookings)

                                binding.tvEmptyState?.visibility =
                                    if (state.bookings.isEmpty()) View.VISIBLE else View.GONE
                            }

                            is BookingHistoryState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                showToast(state.message)
                            }
                        }
                    }
                }

                launch {
                    viewModel.actionState.collect { state ->
                        when (state) {
                            is BookingActionState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }

                            is BookingActionState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                showToast(state.message)
                                viewModel.resetActionState()
                            }

                            is BookingActionState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                showToast(state.message)
                                viewModel.resetActionState()
                            }

                            is BookingActionState.Idle -> Unit
                        }
                    }
                }
            }
        }
    }

    // Menampilkan pesan singkat kepada pelanggan melalui Toast.
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Membersihkan referensi ViewBinding ketika tampilan fragment dihancurkan.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}