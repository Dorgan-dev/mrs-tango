package com.example.mrs_tango.owner.fnb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.mrs_tango.core.data.model.FnbCategoryModel
import com.example.mrs_tango.core.data.model.FnbItemModel
import com.example.mrs_tango.core.data.model.FnbItemWithCategory
import com.example.mrs_tango.databinding.FragmentFnbItemFormBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FnbItemFormFragment(
    private val categories: List<FnbCategoryModel>,
    private val existingItem: FnbItemWithCategory? = null,
    private val onSubmit: (FnbItemModel) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentFnbItemFormBinding? = null
    private val binding get() = _binding!!

    private var selectedCategoryId: String? = null

    // Method ini digunakan untuk menginisialisasi tampilan form tambah atau edit menu FnB menggunakan View Binding.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFnbItemFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Method ini digunakan untuk mengatur pilihan kategori, mengisi data menu saat edit, serta memvalidasi dan mengirim data menu yang disimpan.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryNames = categories.map { it.name }
        binding.actCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
        )

        binding.actCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategoryId = categories.getOrNull(position)?.id
        }

        if (existingItem != null) {
            binding.tvFormTitle.text = "Edit Menu FnB"
            selectedCategoryId = existingItem.categoryId
            val currentCatName = categories.firstOrNull { it.id == existingItem.categoryId }?.name ?: ""
            binding.actCategory.setText(currentCatName, false)
            binding.etItemName.setText(existingItem.name)
            binding.etItemPrice.setText(existingItem.price.toString())
            binding.etItemStock.setText(existingItem.stock.toString())
            binding.switchItemAvailable.isChecked = existingItem.isAvailable
        } else if (categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
            binding.actCategory.setText(categories.first().name, false)
            binding.etItemStock.setText("10")
        }

        binding.btnSaveItem.setOnClickListener {
            val catId = selectedCategoryId
            val name = binding.etItemName.text.toString().trim()
            val price = binding.etItemPrice.text.toString().toIntOrNull() ?: 0
            val stock = binding.etItemStock.text.toString().toIntOrNull() ?: 0

            if (catId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (name.isEmpty()) {
                binding.etItemName.error = "Nama menu wajib diisi"
                return@setOnClickListener
            }
            if (price <= 0) {
                binding.etItemPrice.error = "Harga harus lebih dari 0"
                return@setOnClickListener
            }

            onSubmit(
                FnbItemModel(
                    id = existingItem?.id,
                    categoryId = catId,
                    name = name,
                    price = price,
                    stock = stock.coerceAtLeast(0),
                    isAvailable = binding.switchItemAvailable.isChecked
                )
            )
            dismiss()
        }
    }

    // Method ini digunakan untuk membersihkan View Binding ketika tampilan Bottom Sheet dihancurkan agar mencegah kebocoran memori.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}