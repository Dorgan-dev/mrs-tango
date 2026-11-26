package com.example.mrs_tango.customer

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.mrs_tango.R
import com.example.mrs_tango.core.base.BaseDrawerActivity
import com.example.mrs_tango.core.ui.profile.ProfileFragment
import com.example.mrs_tango.customer.booking.history.BookingHistoryFragment
import com.example.mrs_tango.customer.home.HomeFragment
import com.example.mrs_tango.customer.schedule.ScheduleFragment
import com.example.mrs_tango.databinding.ActivityCustomerBinding

class CustomerActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityCustomerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Wajib dipanggil tepat setelah setContentView & sebelum replaceFragment
        initDrawer(binding.drawerLayout, binding.navigationView)

        // 2. Aman dari crash jika id "main" berubah saat dibungkus DrawerLayout
        findViewById<View>(R.id.main)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                insets
            }
        }

        // 3. Listener Bottom Navigation (cukup satu kali, yang dobel dihapus)
        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.schedule -> {
                    replaceFragment(ScheduleFragment()) // atau BookingHistoryFragment()
                    true
                }
                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // 4. Muat HomeFragment hanya saat pertama kali Activity dibuat
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    override fun onDrawerItemSelected(itemId: Int) {
        when (itemId) {
            R.id.nav_home -> {
                binding.bottomNavView.selectedItemId = R.id.home
            }
            R.id.nav_booking_history -> {
                replaceFragment(BookingHistoryFragment())
            }
            R.id.nav_profile -> {
                binding.bottomNavView.selectedItemId = R.id.profile
            }
        }
    }
}