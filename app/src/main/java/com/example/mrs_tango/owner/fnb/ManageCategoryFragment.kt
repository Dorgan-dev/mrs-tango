package com.example.mrs_tango.owner.fnb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.mrs_tango.core.data.model.FnbCategoryModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class ManageCategoryFragment(
    private val categories: List<FnbCategoryModel>,
    private val onAdd: (String) -> Unit,
    private val onUpdate: (id: String, name: String, isActive: Boolean) -> Unit,
    private val onDelete: (id: String) -> Unit
) : BottomSheetDialogFragment() {

    // Method ini digunakan untuk membuat dan menampilkan tampilan Bottom Sheet yang berisi fitur tambah, ubah status, dan hapus kategori menu.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(requireContext()).apply {
            text = "Kelola Kategori Menu"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        root.addView(title)

        // Baris Tambah Kategori Baru
        val addRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 24, 0, 24)
        }
        val etNewCat = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            hint = "Nama kategori baru..."
        }
        val btnAdd = MaterialButton(requireContext()).apply {
            text = "Tambah"
            setOnClickListener {
                val catName = etNewCat.text.toString().trim()
                if (catName.isEmpty()) {
                    Toast.makeText(requireContext(), "Isi nama kategori", Toast.LENGTH_SHORT).show()
                } else {
                    onAdd(catName)
                    dismiss()
                }
            }
        }
        addRow.addView(etNewCat)
        addRow.addView(btnAdd)
        root.addView(addRow)

        // Daftar Kategori Eksisting
        categories.forEach { cat ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 12, 0, 12)
            }
            val tvName = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                text = cat.name
                textSize = 15f
            }
            val switchActive = SwitchMaterial(requireContext()).apply {
                isChecked = cat.isActive
                setOnCheckedChangeListener { _, checked ->
                    cat.id?.let { onUpdate(it, cat.name, checked) }
                }
            }
            val btnDel = MaterialButton(
                requireContext(),
                null,
                com.google.android.material.R.attr.materialButtonStyle
            ).apply {
                text = "Hapus"

                setOnClickListener {
                    cat.id?.let { onDelete(it) }
                    dismiss()
                }
            }
            row.addView(tvName)
            row.addView(switchActive)
            row.addView(btnDel)
            root.addView(row)
        }

        return root
    }
}