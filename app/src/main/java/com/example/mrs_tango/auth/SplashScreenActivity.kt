package com.example.mrs_tango.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.mrs_tango.R
import com.example.mrs_tango.customer.CustomerActivity
import com.example.mrs_tango.owner.OwnerActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            delay(2000.milliseconds)

            val sharedPref = getSharedPreferences("user_pref", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("isLogin", false)
            val role = sharedPref.getString("role", null)?.trim()?.lowercase()

            Log.d("SPLASH_CHECK", "isLogin=$isLoggedIn, role=$role")

            val targetActivity = if (isLoggedIn) {
                when (role) {
                    "owner" -> OwnerActivity::class.java
                    // "cashier" -> CashierActivity::class.java
                    "customer" -> CustomerActivity::class.java
                    else -> AuthActivity::class.java
                }
            } else {
                AuthActivity::class.java
            }

            startActivity(Intent(this@SplashScreenActivity, targetActivity))
            finish()
        }
    }
}