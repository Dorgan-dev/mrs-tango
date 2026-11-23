package com.example.mrs_tango.customer.home // Sesuaikan package jika berbeda

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.mrs_tango.core.data.DummyData.ConsoleModel
import com.example.mrs_tango.core.data.DummyData.ConsoleAdapter
import com.example.mrs_tango.customer.home.ConsoleDetail.ConsoleDetailFragment // Pastikan import ini benar
import com.example.mrs_tango.R
import com.example.mrs_tango.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val consoleList = listOf(
        ConsoleModel("ID_1", "PlayStation 1", "Rp 3.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_2", "PlayStation 2", "Rp 4.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_3", "PlayStation 3", "Rp 5.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_4", "PlayStation 4", "Rp 10.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_5", "PlayStation 4 Pro", "Rp 12.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_6", "PlayStation 5", "Rp 15.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_7", "PlayStation 5 Digital", "Rp 14.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_8", "PS VR", "Rp 25.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_9", "PS VR 2", "Rp 35.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_10", "PSP", "Rp 5.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_11", "PS Vita", "Rp 7.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_12", "Xbox Original", "Rp 4.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_13", "Xbox 360", "Rp 5.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_14", "Xbox One", "Rp 9.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_15", "Xbox One S", "Rp 10.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_16", "Xbox One X", "Rp 12.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_17", "Xbox Series S", "Rp 14.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_18", "Xbox Series X", "Rp 16.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_19", "Xbox Kinect Sensor", "Rp 8.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_20", "Nintendo NES", "Rp 3.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_21", "Super Nintendo (SNES)", "Rp 4.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_22", "Nintendo 64", "Rp 5.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_23", "Nintendo GameCube", "Rp 6.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_24", "Nintendo Wii", "Rp 7.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_25", "Nintendo Wii U", "Rp 8.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_26", "Nintendo Switch V1", "Rp 10.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_27", "Nintendo Switch V2", "Rp 12.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_28", "Nintendo Switch Lite", "Rp 8.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_29", "Nintendo Switch OLED", "Rp 15.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_30", "Game Boy Advance", "Rp 4.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_31", "Nintendo DS", "Rp 5.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_32", "Nintendo 3DS", "Rp 7.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_33", "Sega Genesis", "Rp 4.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_34", "Sega Dreamcast", "Rp 6.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_35", "Steam Deck 64GB", "Rp 15.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_36", "Steam Deck 256GB", "Rp 18.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_37", "Steam Deck 512GB", "Rp 20.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_38", "Steam Deck OLED", "Rp 25.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_39", "ASUS ROG Ally Z1", "Rp 22.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_40", "ASUS ROG Ally Extreme", "Rp 26.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_41", "Lenovo Legion Go", "Rp 25.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_42", "Meta Quest 2", "Rp 20.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_43", "Meta Quest 3", "Rp 30.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_44", "Meta Quest Pro", "Rp 40.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_45", "HTC Vive", "Rp 25.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_46", "Valve Index", "Rp 35.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_47", "Pico 4 VR", "Rp 22.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_48", "Arcade Machine Retro", "Rp 10.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_49", "Racing Simulator G29", "Rp 30.000 / Jam", R.drawable.ic_launcher_background),
        ConsoleModel("ID_50", "Flight Simulator Setup", "Rp 35.000 / Jam", R.drawable.ic_launcher_background)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "IZIREPS"
        }

        // --- Inisialisasi Adapter dan RecyclerView ---
        val consoleAdapter = ConsoleAdapter(ArrayList(consoleList)) { selectedConsole ->
            val detailFragment = ConsoleDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("EXTRA_CONSOLE", selectedConsole)
                }
            }

            // CARA BARU MEMANGGIL BOTTOM SHEET
            detailFragment.show(parentFragmentManager, "DetailBottomSheet")
        }

        binding.rvConsoles.apply {
            setHasFixedSize(true)
            // Tambahkan baris di bawah jika ingin tampilannya Grid (2 kolom)
            // layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = consoleAdapter
        }
        // -----------------------------------------------------------

//        binding.btnLogout.setOnClickListener {
//            val sharedPref = requireContext().getSharedPreferences("user_pref", MODE_PRIVATE)
//
//            MaterialAlertDialogBuilder(requireContext())
//                .setTitle("Konfirmasi")
//                .setMessage("Apakah anda ingin logout?")
//                .setPositiveButton("Ya") { dialog, _ ->
//                    val editor = sharedPref.edit()
//                    editor.clear()
//                    editor.apply()
//                    dialog.dismiss()
//
//                    val intent = Intent(requireContext(), AuthActivity::class.java)
//                    startActivity(intent)
//                    requireActivity().finish()
//                }
//                .setNegativeButton("Batal") { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .show()
//        }
//
//        binding.btnProfile.setOnClickListener {
//            val i = Intent(requireContext(), ProfileActivity::class.java)
//            startActivity(i)
//        }

        binding.filterIcon.setEndIconOnClickListener {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}