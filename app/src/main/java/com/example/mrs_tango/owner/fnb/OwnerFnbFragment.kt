package com.example.mrs_tango.owner.fnb

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
import com.example.mrs_tango.databinding.FragmentOwnerFnbBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class OwnerFnbFragment : Fragment() {

    private var _binding: FragmentOwnerFnbBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OwnerFnbViewModel by viewModels()
    private lateinit var adapter: OwnerFnbAdapter

    // Method ini digunakan untuk menginisialisasi tampilan fragment menggunakan View Binding.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerFnbBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Method ini digunakan untuk menginisialisasi adapter, mengatur RecyclerView, menangani aksi pengguna, dan memuat seluruh data F&B.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OwnerFnbAdapter(
            onStockDelta = { item, delta -> viewModel.adjustStock(item, delta) },
            onToggleAvailable = { item, available -> viewModel.toggleAvailability(item, available) },
            onEditClick = { item ->
                FnbItemFormFragment(
                    categories = viewModel.getCachedCategories(),
                    existingItem = item,
                    onSubmit = { updated -> viewModel.saveItem(item.id, updated) }
                ).show(parentFragmentManager, "EditFnbItem")
            },
            onDeleteClick = { item ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Hapus Menu?")
                    .setMessage("Menu '${item.name}' akan dihapus permanen.")
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Hapus") { _, _ -> viewModel.deleteItem(item.id) }
                    .show()
            }
        )

        binding.rvOwnerFnbItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOwnerFnbItems.adapter = adapter

        binding.fabAddFnbItem.setOnClickListener {
            val cats = viewModel.getCachedCategories()
            if (cats.isEmpty()) {
                Toast.makeText(requireContext(), "Buat kategori terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FnbItemFormFragment(
                categories = cats,
                existingItem = null,
                onSubmit = { newItem -> viewModel.saveItem(null, newItem) }
            ).show(parentFragmentManager, "AddFnbItem")
        }

        binding.btnManageCategories.setOnClickListener {
            ManageCategoryFragment(
                categories = viewModel.getCachedCategories(),
                onAdd = { name -> viewModel.addCategory(name) },
                onUpdate = { id, name, active -> viewModel.updateCategory(id, name, active) },
                onDelete = { id -> viewModel.deleteCategory(id) }
            ).show(parentFragmentManager, "ManageCategories")
        }

        observeViewModel()
        viewModel.loadAll()
    }

    // Method ini digunakan untuk menampilkan pilihan kategori dalam bentuk Chip dan menangani penyaringan menu berdasarkan kategori yang dipilih.
    private fun renderCategoryChips(categories: List<FnbCategoryModel>, selectedId: String?) {
        binding.chipGroupCategories.removeAllViews()

        val allChip = Chip(requireContext()).apply {
            id = View.generateViewId()
            text = "Semua"
            isCheckable = true
            isChecked = selectedId == null
            setOnClickListener { viewModel.filterByCategory(null) }
        }
        binding.chipGroupCategories.addView(allChip)

        categories.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = if (cat.isActive) cat.name else "${cat.name} (Nonaktif)"
                isCheckable = true
                isChecked = selectedId == cat.id
                setOnClickListener { viewModel.filterByCategory(cat.id) }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    // Method ini digunakan untuk mengamati perubahan data dan status aksi dari ViewModel serta memperbarui tampilan sesuai kondisi yang diterima.
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dataState.collect { state ->
                        when (state) {
                            is OwnerFnbDataState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            is OwnerFnbDataState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                renderCategoryChips(state.categories, state.selectedCategoryId)
                                adapter.submitList(state.filteredItems)
                            }
                            is OwnerFnbDataState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.actionState.collect { state ->
                        when (state) {
                            is OwnerFnbActionState.Success -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetActionState()
                            }
                            is OwnerFnbActionState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                viewModel.resetActionState()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    // Method ini digunakan untuk membersihkan referensi View Binding ketika tampilan fragment dihancurkan agar mencegah kebocoran memori.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}