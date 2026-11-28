package com.example.mrs_tango.owner.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mrs_tango.core.base.BaseDrawerActivity
import com.example.mrs_tango.databinding.FragmentDashboardBinding
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mrs_tango.core.utils.toRupiah
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OwnerDashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Memulai observasi state ViewModel, memuat ringkasan dashboard, dan menyiapkan navigasi drawer.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewModel.loadSummary()

        // Memastikan drawer terbuka saat DashboardFragment ditampilkan
        val baseActivity = requireActivity() as? BaseDrawerActivity
        baseActivity?.unlockDrawer()

        binding.iconMenu.setOnClickListener {
            baseActivity?.openDrawer()
        }
    }

    // Mengamati perubahan state dashboard untuk menampilkan pendapatan, status perangkat, transaksi terbaru, atau pesan error.
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is OwnerDashboardState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is OwnerDashboardState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            val s = state.summary

                            binding.tvTodayRevenue.text = s.todayRevenue.toRupiah()
                            binding.tvTotalAllRevenue.text =
                                "Total Akumulasi: ${s.totalPaidRevenue.toRupiah()} (${s.totalTransactionsCount} Transaksi)"

                            binding.tvCountAvailable.text = s.availableDevices.toString()
                            binding.tvCountActive.text = s.inUseDevices.toString()
                            binding.tvCountMaintenance.text = s.maintenanceDevices.toString()

                            binding.tvRecentTransactions.text = if (s.recentTransactions.isEmpty()) {
                                "Belum ada transaksi selesai."
                            } else {
                                s.recentTransactions.joinToString("\n") { tx ->
                                    "• ${tx.invoiceNumber} (${tx.paymentMethod.uppercase()}) — ${tx.grandTotal.toRupiah()}"
                                }
                            }
                        }
                        is OwnerDashboardState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
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