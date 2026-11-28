package com.example.mrs_tango.customer.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mrs_tango.core.base.BaseDrawerActivity
import com.example.mrs_tango.core.data.repository.SessionRepository
import com.example.mrs_tango.core.utils.Result
import com.example.mrs_tango.customer.home.device_detail.DeviceDetailFragment
import com.example.mrs_tango.databinding.FragmentHomeBinding
import com.example.mrs_tango.owner.device.DeviceListState
import com.example.mrs_tango.owner.device.DeviceViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
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

        binding.rvConsoles.setHasFixedSize(true)
        observeDevices()

        val baseActivity = activity as? BaseDrawerActivity
        baseActivity?.unlockDrawer()
        binding.iconMenu.setOnClickListener { baseActivity?.openDrawer() }
    }

    override fun onResume() {
        super.onResume()
        deviceViewModel.fetchDevices()
        checkActiveSession()
    }

    // Mengamati perubahan daftar perangkat dari ViewModel untuk memperbarui RecyclerView atau menampilkan pesan error.
    private fun observeDevices() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                deviceViewModel.listState.collect { state ->
                    val b = _binding ?: return@collect
                    val ctx = context ?: return@collect
                    when (state) {
                        is DeviceListState.Loading -> Unit
                        is DeviceListState.Success -> {
                            b.rvConsoles.adapter = CustomerDeviceAdapter(state.devices) { selectedDevice ->
                                DeviceDetailFragment.newInstance(selectedDevice)
                                    .show(parentFragmentManager, "DetailBottomSheet")
                            }
                        }
                        is DeviceListState.Error -> {
                            Toast.makeText(ctx, "Gagal memuat perangkat: ${state.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    // Memeriksa sesi bermain aktif milik pelanggan dan menampilkan atau menyembunyikan kartu informasi sesi.
    private fun checkActiveSession() {
        val ctx = context ?: return
        val customerId = ctx.getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .getString("user_id", "") ?: ""
        if (customerId.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            when (val res = SessionRepository().getActiveSessionForCustomer(customerId)) {
                is Result.Success -> {
                    val b = _binding ?: return@launch
                    val session = res.data
                    if (session != null) {
                        b.cardActiveSession.visibility = View.VISIBLE
                        val unitName = session.device?.name ?: "Unit PS"
                        val typeLabel = if (session.sessionType == "free_play") "OPEN BILL" else "PER JAM"
                        val startJam = extractHourMinute(session.startedAt)
                        val endJam = session.plannedEndAt?.let { extractHourMinute(it) }

                        b.tvActiveSessionDevice.text = unitName
                        b.tvActiveSessionType.text = typeLabel
                        b.tvActiveSessionTime.text = if (endJam != null) {
                            "Mulai: $startJam • Estimasi Selesai: $endJam"
                        } else {
                            "Mulai: $startJam • Mode Open Bill"
                        }
                    } else {
                        b.cardActiveSession.visibility = View.GONE
                    }
                }
                is Result.Error -> _binding?.cardActiveSession?.visibility = View.GONE
                else -> Unit
            }
        }
    }

    // Mengambil bagian jam dan menit (HH:mm) dari string timestamp ISO atau tanggal-waktu.
    private fun extractHourMinute(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return "-"
        val timePart = isoTimestamp.substringAfter("T", isoTimestamp.substringAfter(" ", ""))
        return if (timePart.length >= 5) timePart.take(5) else isoTimestamp
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}