package com.example.mrs_tango.auth.login

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mrs_tango.R
import com.example.mrs_tango.auth.RegisterActivity
import com.example.mrs_tango.auth.register.RegisterFragment
import com.example.mrs_tango.customer.CustomerActivity
import com.example.mrs_tango.databinding.ActivityAuthBinding
import com.example.mrs_tango.databinding.FragmentLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Tombol Login
        binding.btnMasuk.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            // Panggil fungsi login di ViewModel (validasi input kosong sudah ditangani di ViewModel)
            viewModel.login(email, password)
        }

        // Tombol Daftar
        binding.btnDaftar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun observeViewModel() {
        val sharedPref = requireContext()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is LoginState.Idle -> {
                            binding.btnMasuk.isEnabled = true
                            binding.btnMasuk.text = "Masuk"
                        }

                        is LoginState.Loading -> {
                            // Ubah tampilan tombol saat proses cek ke Supabase berlangsung
                            binding.btnMasuk.isEnabled = false
                            binding.btnMasuk.text = "Memuat..."
                        }

                        is LoginState.Success -> {
                            binding.btnMasuk.isEnabled = true
                            binding.btnMasuk.text = "Masuk"

                            val user = state.user

                            // Simpan sesi lengkap dari Supabase ke SharedPreferences
                            sharedPref.edit()
                                .putBoolean("isLogin", true)
                                .putString("user_id", user.id)
                                .putString("name", user.name)
                                .putString("email", user.email)
                                .putString("role", user.role)
                                .apply()

                            // Arahkan ke Activity sesuai dengan role yang dikembalikan ViewModel
                            navigateByRole(state.targetDestination)
                        }

                        is LoginState.Error -> {
                            binding.btnMasuk.isEnabled = true
                            binding.btnMasuk.text = "Masuk"

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Login Gagal")
                                .setMessage(state.message)
                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun navigateByRole(destination: String) {
        val intent = when (destination) {
            "ADMIN_HOME" -> {
                Intent(requireContext(), CustomerActivity::class.java) // Sementara
            }
            "STAFF_HOME" -> {
                Intent(requireContext(), CustomerActivity::class.java) // Sementara
            }
            "CUSTOMER_HOME" -> {
                Intent(requireContext(), CustomerActivity::class.java)
            }
            else -> return
        }

        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}