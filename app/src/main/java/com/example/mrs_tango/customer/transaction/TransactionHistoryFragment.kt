package com.example.mrs_tango.customer.transaction

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mrs_tango.core.data.model.TransactionWithDetails
import com.example.mrs_tango.core.utils.toRupiah
import com.example.mrs_tango.databinding.BottomSheetReceiptDetailBinding
import com.example.mrs_tango.databinding.FragmentTransactionHistoryBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class TransactionHistoryFragment : Fragment() {

    private var _binding: FragmentTransactionHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionHistoryViewModel by viewModels()
    private lateinit var adapter: TransactionHistoryAdapter

    // Method ini digunakan untuk menginisialisasi tampilan fragment menggunakan View Binding.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Method ini digunakan untuk mengatur RecyclerView, mengambil ID pelanggan, dan memuat data riwayat transaksi.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TransactionHistoryAdapter { transaction ->
            showReceiptBottomSheet(transaction)
        }
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        val customerId = requireContext()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .getString("user_id", "") ?: ""

        observeViewModel()
        if (customerId.isNotEmpty()) {
            viewModel.fetchTransactions(customerId)
        }
    }

    // Method ini digunakan untuk menampilkan detail bukti transaksi dalam bentuk Bottom Sheet.
    private fun showReceiptBottomSheet(tx: TransactionWithDetails) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetReceiptDetailBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.tvReceiptInvoice.text = "${tx.invoiceNumber} • ${tx.paidAt.replace("T", " ").take(16)}"

        val unitName = tx.session?.device?.name ?: "Unit PS"
        val mins = tx.session?.durationMinutes ?: 0
        sheetBinding.tvReceiptSessionInfo.text = "Unit: $unitName ($mins menit)"
        sheetBinding.tvReceiptGamingCost.text = "Sewa Konsol: ${tx.gamingTotal.toRupiah()}"

        if (tx.items.isNotEmpty()) {
            val fnbLines = tx.items.joinToString("\n") {
                "• ${it.itemName} (${it.quantity}x ${it.unitPrice.toRupiah()}) = ${it.subtotal.toRupiah()}"
            }
            sheetBinding.tvReceiptFnbList.visibility = View.VISIBLE
            sheetBinding.tvReceiptFnbList.text = "Pesanan Makanan & Minuman:\n$fnbLines"
        } else {
            sheetBinding.tvReceiptFnbList.visibility = View.GONE
        }

        sheetBinding.tvReceiptSummary.text = buildString {
            appendLine("Total Tagihan (Grand Total): ${tx.grandTotal.toRupiah()}")
            appendLine("Potongan DP: - ${tx.dpPaid.toRupiah()}")
            appendLine("Sisa Tagihan: ${tx.remainingAmount.toRupiah()}")
            appendLine("Dibayar (${tx.paymentMethod.uppercase()}): ${tx.amountPaid.toRupiah()}")
            append("Kembalian: ${tx.changeAmount.toRupiah()}")
        }

        sheetBinding.btnCloseReceipt.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // Method ini digunakan untuk mengamati perubahan state dari ViewModel dan memperbarui tampilan sesuai kondisi data transaksi.
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is TransactionState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is TransactionState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            adapter.submitList(state.transactions)
                            binding.tvEmptyTransactions.visibility =
                                if (state.transactions.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is TransactionState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    // Method ini digunakan untuk membersihkan View Binding ketika tampilan fragment dihancurkan agar mencegah kebocoran memori.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}