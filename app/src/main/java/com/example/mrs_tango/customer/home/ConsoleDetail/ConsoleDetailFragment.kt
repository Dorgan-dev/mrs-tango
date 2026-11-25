package com.example.mrs_tango.customer.home.ConsoleDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mrs_tango.R // <-- Pastikan ini R milik aplikasimu, BUKAN material.R
import com.example.mrs_tango.core.data.DummyData.ConsoleModel
import com.example.mrs_tango.customer.booking.BookingFragment
import com.example.mrs_tango.databinding.FragmentConsoleDetailBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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

        // Konfigurasi agar Full Screen dan Transparan
        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            // Tulis lengkap com.google.android.material.R agar tidak bentrok dengan R aplikasimu
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )

            bottomSheet?.let {
                it.setBackgroundResource(android.R.color.transparent)

                val layoutParams = it.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.layoutParams = layoutParams

                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        // Ambil data konsol dari arguments
        val console = arguments?.getSerializable("EXTRA_CONSOLE") as? ConsoleModel
        val deviceId = arguments?.getString("EXTRA_DEVICE_ID") ?: ""

        console?.let {
            binding.imgDetail.setImageResource(it.imagePhoto)
            binding.tvDetailName.text = it.name
            binding.tvDetailPrice.text = it.price
        }

        // Tombol pindah ke halaman BookingFragment
        binding.btnBooking.setOnClickListener {
            // Ambil angka saja dari teks harga (misal "Rp 15.000 / jam" -> 15000.0)
            val numericPrice = console?.price
                ?.replace(Regex("[^0-9]"), "")
                ?.toDoubleOrNull() ?: 15000.0

            val bookingFragment = BookingFragment.newInstance(
                deviceId = deviceId, // atau console?.id jika ConsoleModel sudah punya field id
                deviceName = console?.name ?: "Unit",
                pricePerHour = numericPrice
            )

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, bookingFragment)
                .addToBackStack(null)
                .commit()

            dismiss() // Tutup BottomSheet setelah pindah ke BookingFragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}