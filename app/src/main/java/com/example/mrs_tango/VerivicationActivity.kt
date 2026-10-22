package com.example.mrs_tango

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mrs_tango.databinding.ActivityVerivicationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class VerivicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerivicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityVerivicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("user_pref", MODE_PRIVATE)

        val name = sharedPref.getString("reg_name", "") ?: ""
        val email = sharedPref.getString("reg_email", "") ?: ""
        val dob = sharedPref.getString("reg_dob", "") ?: ""
        val gender = sharedPref.getString("reg_gender", "") ?: ""
        val username = sharedPref.getString("reg_username", "") ?: ""
        val password = sharedPref.getString("reg_password", "") ?: ""
        val confirmPassword = sharedPref.getString("reg_confirm_password", "") ?: ""

        // Tampilkan seluruh isian yang tersimpan di SharedPreferences
        binding.tvValName.text = if (name.isNotEmpty()) name else "(Belum diisi)"
        binding.tvValEmail.text = if (email.isNotEmpty()) email else "(Belum diisi)"
        binding.tvValDob.text = if (dob.isNotEmpty()) dob else "(Belum diisi)"
        binding.tvValGender.text = if (gender.isNotEmpty()) gender else "(Belum diisi)"
        binding.tvValUsername.text = if (username.isNotEmpty()) username else "(Belum diisi)"
        binding.tvValPassword.text = if (password.isNotEmpty()) password else "(Belum diisi)"
        binding.tvValConfirmPassword.text = if (confirmPassword.isNotEmpty()) confirmPassword else "(Belum diisi)"

        // Tombol Kembali untuk kembali ke halaman registrasi
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Tombol Submit untuk validasi data
        binding.btnSubmit.setOnClickListener {
            // 1. Validasi: Semua inputan tidak boleh kosong
            if (name.isBlank() || email.isBlank() || dob.isBlank() || gender.isBlank() ||
                username.isBlank() || password.isBlank() || confirmPassword.isBlank()
            ) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Validasi Gagal")
                    .setMessage("Semua inputan tidak boleh kosong! Silakan periksa dan lengkapi data Anda.")
                    .setPositiveButton("Perbaiki") { dialog, _ ->
                        dialog.dismiss()
                        finish() // Kembali ke halaman registrasi
                    }
                    .setNegativeButton("Tutup") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                return@setOnClickListener
            }

            // 2. Validasi: Password dan Confirm Password harus sama
            if (password != confirmPassword) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Validasi Gagal")
                    .setMessage("Password dan Confirm Password tidak sama! Silakan perbaiki terlebih dahulu.")
                    .setPositiveButton("Perbaiki") { dialog, _ ->
                        dialog.dismiss()
                        finish() // Kembali ke halaman registrasi
                    }
                    .setNegativeButton("Tutup") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                return@setOnClickListener
            }

            // Jika berhasil melewati validasi -> simpan data pendaftaran resmi
            val editor = sharedPref.edit()
            editor.putBoolean("is_registered", true)
            editor.putString("registered_username", username)
            editor.putString("registered_password", password)
            editor.apply()

            // Tampilkan halaman informasi "Registrasi Berhasil"
            MaterialAlertDialogBuilder(this)
                .setTitle("Registrasi Berhasil")
                .setMessage("Selamat, registrasi Anda telah berhasil! Silakan login menggunakan akun yang telah terdaftar.")
                .setCancelable(false)
                .setPositiveButton("Ke Halaman Login") { dialog, _ ->
                    dialog.dismiss()

                    // Kembali ke Halaman Login (AuthActivity)
                    val intent = Intent(this@VerivicationActivity, AuthActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .show()
        }
    }
}