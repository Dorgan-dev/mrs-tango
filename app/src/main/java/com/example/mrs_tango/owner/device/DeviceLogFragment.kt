package com.example.mrs_tango.owner.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mrs_tango.databinding.FragmentDeviceLogBinding
import kotlinx.coroutines.launch

class DeviceLogFragment : Fragment() {

    private var _binding: FragmentDeviceLogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OwnerDeviceViewModel by viewModels()
    private val adapter = DeviceLogAdapter()

    // Menginflasi layout fragment dan mengembalikan root view untuk ditampilkan.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Menyiapkan RecyclerView, memulai observasi data log, dan memicu pengambilan log terbaru.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeLogs()
        viewModel.fetchLogs()
    }

    // Mengatur layout manager dan adapter pada RecyclerView daftar log perangkat.
    private fun setupRecyclerView() = with(binding.rvDeviceLogs) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@DeviceLogFragment.adapter
    }

    // Mengamati perubahan logsState secara aman sesuai siklus hidup view dan memperbarui tampilan daftar.
    private fun observeLogs() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logsState.collect { logs ->
                    adapter.submitList(logs)
                    binding.tvEmptyLogs.isVisible = logs.isEmpty()
                }
            }
        }
    }

    // Membersihkan referensi view binding saat view fragment dihancurkan untuk mencegah memory leak.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}