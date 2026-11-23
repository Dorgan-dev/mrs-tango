package com.example.mrs_tango.customer.home.ConsoleDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mrs_tango.core.data.DummyData.ConsoleModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.mrs_tango.databinding.FragmentConsoleDetailBinding
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ConsoleDetailFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentConsoleDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsoleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Konfigurasi agar Full Screen dan Trans   paran (supaya lengkungan terlihat)
        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                R.id.design_bottom_sheet
            )

            bottomSheet?.let {
                // Hapus background bawaan sistem yang kotak
                it.setBackgroundResource(android.R.color.transparent)

                // Ubah tinggi bottom sheet menjadi setinggi layar (Match Parent)
                val layoutParams = it.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.layoutParams = layoutParams

                // Paksa statusnya menjadi otomatis terbuka penuh (Expanded)
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true // Mencegah tersangkut di tengah saat di-swipe ke bawah
            }
        }

        // Ambil data konsol dari arguments
        val console = arguments?.getSerializable("EXTRA_CONSOLE") as? ConsoleModel
        console?.let {
            binding.imgDetail.setImageResource(it.imagePhoto)
            binding.tvDetailName.text = it.name
            binding.tvDetailPrice.text = it.price
        }

        // Tampilkan Date Picker
        binding.etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal Booking")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                binding.etDate.setText(formatter.format(Date(selection)))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        // Tampilkan Time Picker
        binding.etTime.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Pilih Jam Main")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", timePicker.hour, timePicker.minute)
                binding.etTime.setText(formattedTime)
            }

            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        // Tombol Submit Pemesanan
        binding.btnSubmitBooking.setOnClickListener {
            val date = binding.etDate.text.toString()
            val time = binding.etTime.text.toString()

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), "Harap lengkapi tanggal dan jam!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Booking ${console?.name} Berhasil untuk $date jam $time", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack() // Kembali ke halaman Home
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}