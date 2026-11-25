package com.example.mrs_tango.customer.home // Sesuaikan package jika berbeda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mrs_tango.R
import com.example.mrs_tango.core.data.DummyData.ConsoleAdapter
import com.example.mrs_tango.core.data.DummyData.ConsoleModel
import com.example.mrs_tango.core.data.model.DeviceModel
import com.example.mrs_tango.customer.home.ConsoleDetail.ConsoleDetailFragment
import com.example.mrs_tango.databinding.FragmentHomeBinding
import com.example.mrs_tango.owner.device.DeviceListState
import com.example.mrs_tango.owner.device.DeviceViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Pakai ulang DeviceViewModel yang sudah terhubung ke tabel devices di Supabase
    private val deviceViewModel: DeviceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "IZIREPS"
        }

        binding.rvConsoles.setHasFixedSize(true)
        // Aktifkan baris di bawah jika di fragment_home.xml belum ada app:layoutManager
        // binding.rvConsoles.layoutManager = GridLayoutManager(requireContext(), 2)

        observeDevices()

        binding.filterIcon.setEndIconOnClickListener {
            // Aksi filter jika diperlukan nanti
        }
    }

    override fun onResume() {
        super.onResume()
        // Muat ulang data setiap kali kembali ke Home agar status perangkat selalu terbaru
        deviceViewModel.fetchDevices()
    }

    private fun observeDevices() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                deviceViewModel.listState.collect { state ->
                    when (state) {
                        is DeviceListState.Loading -> {
                            // Tampilkan ProgressBar di sini jika ada di fragment_home.xml
                        }

                        is DeviceListState.Success -> {
                            // Ubah daftar DeviceModel (Supabase) ke ConsoleModel agar cocok dengan ConsoleAdapter
                            // Jika ingin hanya menampilkan unit yang tersedia, tambahkan:
                            // .filter { it.status.equals("available", ignoreCase = true) }
                            val mappedList = state.devices.map { device ->
                                mapDeviceToConsole(device)
                            }

                            val consoleAdapter =
                                ConsoleAdapter(ArrayList(mappedList)) { selectedConsole ->
                                    val detailFragment = ConsoleDetailFragment().apply {
                                        arguments = Bundle().apply {
                                            // Kirim UUID asli dari tabel devices & objek ConsoleModel
                                            putString("EXTRA_DEVICE_ID", selectedConsole.id)
                                            putSerializable("EXTRA_CONSOLE", selectedConsole)
                                        }
                                    }
                                    detailFragment.show(parentFragmentManager, "DetailBottomSheet")
                                }

                            binding.rvConsoles.adapter = consoleAdapter
                        }

                        is DeviceListState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                "Gagal memuat perangkat: ${state.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun mapDeviceToConsole(device: DeviceModel): ConsoleModel {
        val displayName = buildString {
            append(device.name ?: "Unit")
            if (!device.consoleType.isNullOrBlank()) {
                append(" (${device.consoleType})")
            }
        }

        return ConsoleModel(
            id = device.id ?: "",
            name = displayName,
            price = getDefaultPriceByType(device.consoleType),
            imagePhoto = R.drawable.ic_launcher_background
        )
    }

    // Penentuan tarif sementara berdasarkan tipe konsol (karena tabel devices belum punya kolom price)
    private fun getDefaultPriceByType(consoleType: String?): String {
        return when {
            consoleType?.contains("PS5", ignoreCase = true) == true -> "Rp 15.000 / Jam"
            consoleType?.contains("PS4", ignoreCase = true) == true -> "Rp 10.000 / Jam"
            consoleType?.contains("Xbox", ignoreCase = true) == true -> "Rp 14.000 / Jam"
            consoleType?.contains("Switch", ignoreCase = true) == true -> "Rp 12.000 / Jam"
            else -> "Rp 10.000 / Jam"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}