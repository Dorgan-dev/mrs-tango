package com.example.mrs_tango.customer.home

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

        binding.filterIcon.setEndIconOnClickListener {
            // Aksi filter jika diperlukan nanti
        }

        val baseActivity = requireActivity() as? BaseDrawerActivity
        baseActivity?.unlockDrawer()

        binding.iconMenu.setOnClickListener {
            baseActivity?.openDrawer()
        }
    }

    override fun onResume() {
        super.onResume()
        deviceViewModel.fetchDevices()
    }

    private fun observeDevices() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                deviceViewModel.listState.collect { state ->
                    when (state) {
                        is DeviceListState.Loading -> {
                            // Tampilkan ProgressBar di sini jika ada
                        }

                        is DeviceListState.Success -> {
                            val deviceAdapter = CustomerDeviceAdapter(state.devices) { selectedDevice ->
                                val detailFragment = DeviceDetailFragment.newInstance(selectedDevice)
                                detailFragment.show(parentFragmentManager, "DetailBottomSheet")
                            }

                            binding.rvConsoles.adapter = deviceAdapter
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}