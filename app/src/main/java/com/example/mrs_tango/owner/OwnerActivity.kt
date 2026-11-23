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

class OwnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            when (item.itemId) { // Ganti ID di bawah ini sesuai dengan ID yang ada di res/menu/bottom_nav_owner.xml
                R.id.dashboard -> binding.viewPager.currentItem = 0
                R.id.report -> binding.viewPager.currentItem = 1
                R.id.device -> binding.viewPager.currentItem = 2
                R.id.cashier -> binding.viewPager.currentItem = 3
            }
            true
        }
    }
}