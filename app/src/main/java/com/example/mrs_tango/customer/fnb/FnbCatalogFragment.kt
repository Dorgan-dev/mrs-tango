package com.example.mrs_tango.customer.fnb

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
import com.example.mrs_tango.core.data.model.FnbCategoryModel
import com.example.mrs_tango.databinding.FragmentFnbCatalogBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class FnbCatalogFragment : Fragment() {

    private var _binding: FragmentFnbCatalogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FnbCatalogViewModel by viewModels()
    private val adapter = FnbCatalogAdapter()
    private var chipsPopulated = false

    // Meng-inflate layout XML fragment dan mengembalikan root view-nya.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFnbCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Mengatur RecyclerView, memulai observasi state ViewModel, dan memicu pemuatan data katalog.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvFnbItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFnbItems.adapter = adapter

        observeViewModel()
        viewModel.loadCatalog()
    }

    // Membuat chip filter kategori secara dinamis dan menghubungkannya ke aksi pemilihan kategori di ViewModel.
    private fun setupCategoryChips(categories: List<FnbCategoryModel>, selectedId: String?) {
        if (chipsPopulated) return
        chipsPopulated = true

        binding.chipGroupCategories.removeAllViews()

        val allChip = Chip(requireContext()).apply {
            id = View.generateViewId()
            text = "Semua"
            isCheckable = true
            isChecked = selectedId == null
            setOnClickListener { viewModel.selectCategory(null) }
        }
        binding.chipGroupCategories.addView(allChip)

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = category.name
                isCheckable = true
                isChecked = selectedId == category.id
                setOnClickListener { viewModel.selectCategory(category.id) }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    // Mengamati perubahan FnbCatalogState saat lifecycle STARTED untuk memperbarui UI, daftar menu, dan pesan error.
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is FnbCatalogState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is FnbCatalogState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            setupCategoryChips(state.categories, state.selectedCategoryId)
                            adapter.submitList(state.filteredItems)
                            binding.tvEmptyFnb.visibility =
                                if (state.filteredItems.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is FnbCatalogState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    // Mereset flag chip dan membersihkan referensi view binding saat view fragment dihancurkan.
    override fun onDestroyView() {
        super.onDestroyView()
        chipsPopulated = false
        _binding = null
    }
}