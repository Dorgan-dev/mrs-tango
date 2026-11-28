package com.example.mrs_tango.owner

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.mrs_tango.R
import com.example.mrs_tango.databinding.ActivityOwnerBinding
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.mrs_tango.core.base.BaseDrawerActivity
import com.example.mrs_tango.customer.booking.history.BookingHistoryFragment

class OwnerActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityOwnerBinding

    // Menginisialisasi binding layout, navigation drawer, serta sinkronisasi ViewPager2 dan BottomNavigationView.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initDrawer(binding.drawerLayout, binding.navigationView)
        setupViewPagerAndBottomNav()
    }

    private fun setupViewPagerAndBottomNav() {
        val pagerAdapter = OwnerPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNavView.menu[position].isChecked = true
            }
        })

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> binding.viewPager.currentItem = 0
                R.id.report -> binding.viewPager.currentItem = 1
                R.id.device -> binding.viewPager.currentItem = 2
                R.id.cashier -> binding.viewPager.currentItem = 3
                R.id.fnb -> binding.viewPager.currentItem = 4
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.viewPager.id, fragment)
            .commit()
    }

    // Mengarahkan navigasi ke halaman atau tab terkait saat item pada drawer dipilih.
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