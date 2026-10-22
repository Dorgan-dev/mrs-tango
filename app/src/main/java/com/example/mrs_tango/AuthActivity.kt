package com.example.mrs_tango

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mrs_tango.databinding.ActivityAuthBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("user_pref", MODE_PRIVATE)

        // Tombol Login
        binding.btnLogin.setOnClickListener {
            val username = binding.inputUsername.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            val isRegistered = sharedPref.getBoolean("is_registered", false)
            val regUsername = sharedPref.getString("registered_username", "") ?: ""
            val regPassword = sharedPref.getString("registered_password", "") ?: ""

            // Rule 1: Jika username == password -> Boleh Login
            val isRule1Valid = username.isNotEmpty() && username == password

            // Rule 2: Jika username == username di SharedPreferences dan password == password di SharedPreferences -> Boleh Login
            val isRule2Valid = isRegistered && username.isNotEmpty() && password.isNotEmpty() &&
                    username == regUsername && password == regPassword

            if (isRule1Valid || isRule2Valid) {
                val editor = sharedPref.edit()
                editor.putBoolean("isLogin", true)
                editor.putString("username", username)
                editor.apply()

                val intent = Intent(this@AuthActivity, BaseActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Login gagal")
                    .setMessage("Username atau password salah. Silakan coba lagi atau daftarkan akun baru.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }

        // Tombol Registrasi Akun Baru
        binding.btnRegister.setOnClickListener {
            val intent = Intent(this@AuthActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}