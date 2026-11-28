package com.example.mrs_tango.customer

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.mrs_tango.R
import com.example.mrs_tango.core.base.BaseDrawerActivity
import com.example.mrs_tango.customer.profile.CustomerProfileFragment
import com.example.mrs_tango.customer.home.HomeFragment
import com.example.mrs_tango.customer.schedule.ScheduleFragment
import com.example.mrs_tango.databinding.ActivityCustomerBinding
import androidx.core.graphics.Insets
import com.example.mrs_tango.customer.fnb.FnbCatalogFragment
import com.example.mrs_tango.customer.transaction.TransactionHistoryFragment

class CustomerActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityCustomerBinding

    // Menginisialisasi layout edge-to-edge, inset sistem, drawer, dan navigasi utama saat Activity dibuat.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = systemBars.top
            params.leftMargin = systemBars.left
            params.rightMargin = systemBars.right
            v.layoutParams = params

            WindowInsetsCompat.Builder(insets)
                .setInsets(
                    WindowInsetsCompat.Type.systemBars(),
                    Insets.of(0, 0, 0, systemBars.bottom)
                )
                .build()
        }

        initDrawer(binding.drawerLayout, binding.navigationView)

        findViewById<View>(R.id.main)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                insets
            }
        }

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.schedule -> {
                    replaceFragment(ScheduleFragment())
                    true
                }
                R.id.profile -> {
                    replaceFragment(CustomerProfileFragment())
                    true
                }
                else -> false
            }
        }
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    // Mengarahkan tampilan ke fragment atau tab navigasi sesuai item menu drawer yang dipilih.
    override fun onDrawerItemSelected(itemId: Int) {
        when (itemId) {
            R.id.nav_home -> {
                binding.bottomNavView.selectedItemId = R.id.home
            }
            R.id.nav_booking_history -> {
                binding.bottomNavView.selectedItemId = R.id.schedule
            }
            R.id.nav_fnb_catalog -> {
                replaceFragment(FnbCatalogFragment())
            }
            R.id.nav_transactions -> {
                replaceFragment(TransactionHistoryFragment())
            }
            R.id.nav_profile -> {
                binding.bottomNavView.selectedItemId = R.id.profile
            }
        }
    }
}