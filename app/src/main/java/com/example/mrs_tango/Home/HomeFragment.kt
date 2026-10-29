package com.example.mrs_tango.Home

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.mrs_tango.AuthActivity
import com.example.mrs_tango.DummyData.Console
import com.example.mrs_tango.DummyData.ConsoleAdapter
import com.example.mrs_tango.Home.ConsoleDetail.DetailConsoleFragment
import com.example.mrs_tango.ProfileActivity
import com.example.mrs_tango.R
import com.example.mrs_tango.databinding.FragmentHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Penampung data dummy
    private val list = ArrayList<Console>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Home"
        }

        // --- Inisialisasi RecyclerView ---
        binding.rvConsoles.setHasFixedSize(true)
        list.addAll(getDummyData())
        showRecyclerGrid()
        // ---------------------------------

        binding.btnLogout.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences("user_pref", MODE_PRIVATE)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda ingin logout?")
                .setPositiveButton("Ya") { dialog, _ ->
                    val editor = sharedPref.edit()
                    editor.clear()
                    editor.apply()
                    dialog.dismiss()

                    val intent = Intent(requireContext(), AuthActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnProfile.setOnClickListener {
            val i = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(i)
        }
    }

    private fun getDummyData(): ArrayList<Console> {
        val dataName = arrayOf("PlayStation 3", "PlayStation 4", "PlayStation 5", "PS VR", "ps gue")
        val dataPrice = arrayOf("Rp 5.000 / Jam", "Rp 10.000 / Jam", "Rp 15.000 / Jam", "Rp 25.000 / Jam", "Rp 21.000 / Jam")

        // Ganti R.drawable.ic_launcher_background dengan gambar PS asli Anda (misal: R.drawable.gambar_ps3)
        val dataPhoto = intArrayOf(
            R.drawable.ps5_u1,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background
        )

        val listConsole = ArrayList<Console>()
        for (i in dataName.indices) {
            val console = Console("ID_${i+1}", dataName[i], dataPrice[i], dataPhoto[i])
            listConsole.add(console)
        }
        return listConsole
    }

    private fun showRecyclerGrid() {
        val consoleAdapter = ConsoleAdapter(list) { selectedConsole ->
            // Buat instance DetailFragment dan kirim data console
            val detailFragment = DetailConsoleFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("EXTRA_CONSOLE", selectedConsole)
                }
            }

            // Pindah Fragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment) // Ganti R.id.fragment_container sesuai ID FrameLayout/FragmentContainerView di Activity utama Anda
                .addToBackStack(null)
                .commit()
        }
        binding.rvConsoles.adapter = consoleAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Mencegah memory leak
    }
}