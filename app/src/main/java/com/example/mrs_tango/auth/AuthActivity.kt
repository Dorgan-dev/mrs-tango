package com.example.mrs_tango.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mrs_tango.auth.login.LoginFragment
import com.example.mrs_tango.databinding.ActivityAuthBinding


class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menampilkan LoginFragment sebagai fragment utama
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, LoginFragment())
                .commit()
        }
    }
}
