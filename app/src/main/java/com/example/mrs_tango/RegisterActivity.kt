package com.example.mrs_tango

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mrs_tango.databinding.ActivityRegisterBinding
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("user_pref", MODE_PRIVATE)

        // Pre-fill data if previously stored in SharedPreferences
        binding.inputName.setText(sharedPref.getString("reg_name", ""))
        binding.inputEmail.setText(sharedPref.getString("reg_email", ""))
        binding.inputDob.setText(sharedPref.getString("reg_dob", ""))
        binding.inputUsername.setText(sharedPref.getString("reg_username", ""))
        binding.inputPassword.setText(sharedPref.getString("reg_password", ""))
        binding.inputConfirmPassword.setText(sharedPref.getString("reg_confirm_password", ""))

        val savedGender = sharedPref.getString("reg_gender", "")
        if (savedGender == "Laki-laki") {
            binding.rbMale.isChecked = true
        } else if (savedGender == "Perempuan") {
            binding.rbFemale.isChecked = true
        }

        // Setup DatePicker for inputDob
        binding.inputDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                    binding.inputDob.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        // Handle Tombol Selanjutnya
        binding.btnNext.setOnClickListener {
            val name = binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val dob = binding.inputDob.text.toString().trim()
            val username = binding.inputUsername.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val confirmPassword = binding.inputConfirmPassword.text.toString().trim()

            val selectedGenderId = binding.rgGender.checkedRadioButtonId
            val gender = when (selectedGenderId) {
                R.id.rbMale -> "Laki-laki"
                R.id.rbFemale -> "Perempuan"
                else -> ""
            }

            // Simpan setiap isian ke SharedPreferences untuk dicek di halaman verifikasi
            val editor = sharedPref.edit()
            editor.putString("reg_name", name)
            editor.putString("reg_email", email)
            editor.putString("reg_dob", dob)
            editor.putString("reg_gender", gender)
            editor.putString("reg_username", username)
            editor.putString("reg_password", password)
            editor.putString("reg_confirm_password", confirmPassword)
            editor.apply()

            val intent = Intent(this@RegisterActivity, VerivicationActivity::class.java)
            startActivity(intent)
        }
    }
}