package com.example.mrs_tango.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mrs_tango.R
import com.example.mrs_tango.auth.login.LoginFragment
import com.example.mrs_tango.databinding.FragmentRegisterBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val vm: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Tampilkan Date Picker
        binding.inputBirthDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal Lahir")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Supabase butuh format YYYY-MM-DD
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                binding.inputBirthDate.setText(formatter.format(Date(selection)))
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        // 2. Tombol Daftar (Hanya lempar data ke VM)
        binding.btnDaftar.setOnClickListener {
            val name = binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val birthDate = binding.inputBirthDate.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val confirmPassword = binding.inputConfirmPassword.text.toString().trim()
            val gender = getSelectedGender()

            // Biarkan ViewModel yang melakukan validasi dan pemrosesan
            vm.registerCustomer(name, email, gender, birthDate, password, confirmPassword)
        }

        // 3. Observe hasil dari ViewModel (StateFlow)
        viewLifecycleOwner.lifecycleScope.launch {
            vm.registerState.collect { state ->
                when (state) {
                    is RegisterState.Idle -> {
                        binding.btnDaftar.isEnabled = true
                        binding.btnDaftar.text = "Daftar"
                    }

                    is RegisterState.Loading -> {
                        binding.btnDaftar.isEnabled = false
                        binding.btnDaftar.text = "Memproses..."
                    }

                    is RegisterState.Success -> {
                        binding.btnDaftar.isEnabled = true
                        binding.btnDaftar.text = "Daftar"
                        Toast.makeText(requireContext(), "Registrasi berhasil!", Toast.LENGTH_SHORT).show()

                        vm.resetState()

                        // Navigasi ke Login
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, LoginFragment())
                            .commit()
                    }

                    is RegisterState.Error -> {
                        binding.btnDaftar.isEnabled = true
                        binding.btnDaftar.text = "Daftar"

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Peringatan")
                            .setMessage(state.message)
                            .setPositiveButton("OK") { d, _ -> d.dismiss() }
                            .show()

                        vm.resetState()
                    }
                }
            }
        }

        // 4. Sudah punya akun? Login
        binding.btnMasuk.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getSelectedGender(): String {
        return when (binding.chipGroupGender.checkedChipId) {
            R.id.chipMale -> "Laki-laki"
            R.id.chipFemale -> "Perempuan"
            else -> ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}