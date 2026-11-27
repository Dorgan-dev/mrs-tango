package com.example.mrs_tango.customer.profile

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.mrs_tango.R
import com.example.mrs_tango.core.data.model.UserModel
import com.example.mrs_tango.core.utils.ImageCompressor
import com.example.mrs_tango.customer.booking.history.BookingHistoryFragment
import com.example.mrs_tango.databinding.BottomSheetEditProfileBinding
import com.example.mrs_tango.databinding.FragmentCustomerProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import androidx.core.content.edit

class CustomerProfileFragment : Fragment() {

    private var _binding: FragmentCustomerProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private var currentUserId = ""
    private var currentUser: UserModel? = null
    private var selectedImageUri: Uri? = null
    private var editDialog: BottomSheetDialog? = null
    private var editSheetBinding: BottomSheetEditProfileBinding? = null

    private val pickAvatarLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            editSheetBinding?.ivEditAvatar?.load(uri) { crossfade(true) }
            binding.ivAvatar.load(uri) { crossfade(true) }
        }
    }

    // Membuat tampilan halaman profil pengguna.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Menginisialisasi data pengguna, listener, dan pengamatan ViewModel.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        currentUserId = prefs.getString("user_id", "") ?: ""

        binding.tvName.text = prefs.getString("user_name", "Pengguna")
        binding.tvEmail.text = prefs.getString("user_email", "-")

        setupListeners()
        observeViewModel()

        if (currentUserId.isNotEmpty()) {
            viewModel.fetchProfile(currentUserId)
        }
    }

    // Mengatur aksi tombol dan navigasi pada halaman profil.
    private fun setupListeners() {
        binding.iconMenu.setOnClickListener {
            activity?.findViewById<DrawerLayout>(R.id.drawerLayout)
                ?.openDrawer(GravityCompat.START)
        }

        binding.btnEditProfile.setOnClickListener { showEditProfileBottomSheet() }
        binding.cvAvatar.setOnClickListener { showEditProfileBottomSheet() }
        binding.menuAkun.setOnClickListener { showEditProfileBottomSheet() }

        binding.menuRiwayatBooking.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookingHistoryFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Yakin ingin keluar dari akun ini?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Logout") { _, _ ->
                    requireContext()
                        .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                        .edit {
                            clear()
                        }
                }
                .show()
        }

        binding.menuPengaturan.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur Pengaturan segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.menuPoinRoyalti.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur Poin Royalti segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.menuSyaratKetentuan.setOnClickListener {
            Toast.makeText(requireContext(), "Syarat & Ketentuan Izireps", Toast.LENGTH_SHORT).show()
        }

        binding.menuBantuan.setOnClickListener {
            Toast.makeText(requireContext(), "Hubungi admin rental untuk bantuan", Toast.LENGTH_SHORT).show()
        }
    }

    // Menampilkan BottomSheet untuk mengubah informasi profil pengguna.
    private fun showEditProfileBottomSheet() {
        val user = currentUser

        if (user == null && currentUserId.isEmpty()) {
            Toast.makeText(requireContext(), "Sesi login tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        selectedImageUri = null

        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetEditProfileBinding.inflate(layoutInflater)

        dialog.setContentView(sheetBinding.root)

        editDialog = dialog
        editSheetBinding = sheetBinding

        sheetBinding.etEditName.setText(user?.name ?: binding.tvName.text.toString())
        sheetBinding.etEditPhone.setText(user?.phone ?: "")

        if (!user?.avatar.isNullOrBlank()) {
            sheetBinding.ivEditAvatar.load(user?.avatar) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_myplaces)
                error(android.R.drawable.ic_menu_myplaces)
            }
        }

        val pickAction = {
            pickAvatarLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        sheetBinding.cvEditAvatar.setOnClickListener { pickAction() }
        sheetBinding.tvPickPhoto.setOnClickListener { pickAction() }

        sheetBinding.btnSimpanProfil.setOnClickListener {
            val newName = sheetBinding.etEditName.text.toString().trim()
            val newPhone = sheetBinding.etEditPhone.text.toString().trim()

            if (newName.isEmpty()) {
                sheetBinding.etEditName.error = "Nama lengkap wajib diisi"
                return@setOnClickListener
            }

            val avatarBytes = selectedImageUri?.let {
                ImageCompressor.compressUri(
                    context = requireContext(),
                    uri = it,
                    maxDimension = 512,
                    quality = 75
                )
            }

            viewModel.updateProfile(
                userId = currentUserId,
                name = newName,
                phone = newPhone,
                newAvatarBytes = avatarBytes,
                oldAvatarUrl = user?.avatar
            )
        }

        dialog.setOnDismissListener {
            editDialog = null
            editSheetBinding = null
        }

        dialog.show()
    }

    // Mengamati perubahan data profil dan proses pembaruan profil.
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.profileState.collect { state ->
                        when (state) {
                            is ProfileState.Loading -> {}
                            is ProfileState.Success -> {
                                val user = state.user
                                currentUser = user

                                binding.tvName.text = user.name
                                binding.tvEmail.text = user.email

                                if (!user.avatar.isNullOrBlank()) {
                                    binding.ivAvatar.load(user.avatar) {
                                        crossfade(true)
                                        placeholder(android.R.drawable.ic_menu_myplaces)
                                        error(android.R.drawable.ic_menu_myplaces)
                                    }
                                }
                            }
                            is ProfileState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.updateState.collect { state ->
                        when (state) {
                            is ProfileUpdateState.Loading -> {
                                editSheetBinding?.progressEdit?.visibility = View.VISIBLE
                                editSheetBinding?.btnSimpanProfil?.isEnabled = false
                            }

                            is ProfileUpdateState.Success -> {
                                editSheetBinding?.progressEdit?.visibility = View.GONE
                                editSheetBinding?.btnSimpanProfil?.isEnabled = true

                                currentUser = state.user
                                selectedImageUri = null

                                requireContext()
                                    .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                                    .edit {
                                        putString("user_name", state.user.name)
                                    }

                                activity?.findViewById<NavigationView>(R.id.navigationView)
                                    ?.getHeaderView(0)
                                    ?.findViewById<TextView>(R.id.tvHeaderName)
                                    ?.text = state.user.name

                                editDialog?.dismiss()

                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetUpdateState()
                            }

                            is ProfileUpdateState.Error -> {
                                editSheetBinding?.progressEdit?.visibility = View.GONE
                                editSheetBinding?.btnSimpanProfil?.isEnabled = true

                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                viewModel.resetUpdateState()
                            }

                            is ProfileUpdateState.Idle -> {}
                        }
                    }
                }
            }
        }
    }

    // Membersihkan binding dan BottomSheet ketika tampilan dihancurkan.
    override fun onDestroyView() {
        super.onDestroyView()
        editDialog?.dismiss()
        _binding = null
    }
}
