package com.example.mrs_tango.customer.home.device_detail

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.example.mrs_tango.R
import com.example.mrs_tango.core.data.model.DeviceDetailsModel
import com.example.mrs_tango.core.data.model.DeviceModel
import com.example.mrs_tango.customer.booking.BookingFragment
import com.example.mrs_tango.databinding.FragmentDeviceDetailBinding
import com.google.android.material.R as MaterialR
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DeviceDetailFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDeviceDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_DEVICE = "EXTRA_DEVICE"
        private const val ARG_CONSOLE = "EXTRA_CONSOLE"
        private const val ARG_DEVICE_ID = "EXTRA_DEVICE_ID"

        fun newInstance(device: DeviceDetailsModel): DeviceDetailFragment {
            return DeviceDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DEVICE, device)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFullScreenBottomSheet()

        val deviceDetailsModel = getSerializableArg(ARG_DEVICE, DeviceDetailsModel::class.java)
        val console = getSerializableArg(ARG_CONSOLE, DeviceModel::class.java)
        val deviceId = deviceDetailsModel?.id
            ?: arguments?.getString(ARG_DEVICE_ID)
            ?: ""

        val name = deviceDetailsModel?.displayName ?: console?.name ?: "Unit"
        val pricePerHour = deviceDetailsModel?.activePricePerHour
            ?.takeIf { it > 0 }?.toDouble()
            ?: console?.price?.toDouble()
            ?: 15000.0

        // Tampilkan detail perangkat
        if (deviceDetailsModel != null) {
            binding.tvDetailName.text = deviceDetailsModel.displayName

            // Jika punya lebih dari 1 shift (misal Siang & Malam), tampilkan tarif aktif + jadwalnya
            // Catatan: Kalau nanti kamu buat TextView terpisah (misal tvRateSchedule), pisahkan ke sana.
            binding.tvDetailPrice.text = if (deviceDetailsModel.activeRates.size > 1) {
                "${deviceDetailsModel.formattedPrice}\n${deviceDetailsModel.formattedRateSchedule}"
            } else {
                deviceDetailsModel.formattedPrice
            }

            binding.imgDetail.load(deviceDetailsModel.primaryImageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
                fallback(R.drawable.ic_launcher_background)
            }
        } else if (console != null) {
            binding.tvDetailName.text = console.name
            binding.tvDetailPrice.text = "Rp ${console.price} / jam"
        }

        // Tombol pindah ke halaman BookingFragment
        binding.btnBooking.setOnClickListener {
            val bookingFragment = BookingFragment.newInstance(
                deviceId = deviceId,
                deviceName = name,
                pricePerHour = pricePerHour
            )

            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, bookingFragment)
                .addToBackStack(null)
                .commit()

            dismiss()
        }
    }

    private fun setupFullScreenBottomSheet() {
        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                MaterialR.id.design_bottom_sheet
            )

            bottomSheet?.let {
                it.setBackgroundResource(android.R.color.transparent)
                it.layoutParams = it.layoutParams.apply {
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }

                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun <T : java.io.Serializable> getSerializableArg(key: String, clazz: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(key, clazz)
        } else {
            arguments?.getSerializable(key) as? T
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}