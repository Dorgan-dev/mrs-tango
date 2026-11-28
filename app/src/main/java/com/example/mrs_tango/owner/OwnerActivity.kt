package com.example.mrs_tango.owner

import android.os.Bundle
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.mrs_tango.R
import com.example.mrs_tango.core.base.BaseDrawerActivity
import com.example.mrs_tango.customer.booking.history.BookingHistoryFragment
import com.example.mrs_tango.databinding.ActivityOwnerBinding
import com.example.mrs_tango.owner.device.DeviceLogFragment
import com.example.mrs_tango.owner.fnb.OwnerFnbFragment

class OwnerActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityOwnerBinding

    private val navToPageIndex = mapOf(
        R.id.dashboard to 0,
        R.id.report to 1,
        R.id.device to 2,
        R.id.cashier to 3,
        R.id.fnb to 4
    )

    // Menginisialisasi view binding, drawer navigasi, serta sinkronisasi ViewPager2 dan bottom navigation.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerBinding.inflate(layoutInflater).also { setContentView(it.root) }
        initDrawer(binding.drawerLayout, binding.navigationView)
        setupViewPagerAndBottomNav()
    }

    // Menghubungkan adapter ViewPager2 dengan perubahan halaman dan pilihan item pada bottom navigation.
    private fun setupViewPagerAndBottomNav() = with(binding) {
        viewPager.adapter = OwnerPagerAdapter(this@OwnerActivity)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position in 0 until bottomNavView.menu.size()) {
                    bottomNavView.menu[position].isChecked = true
                }
            }
        })

        bottomNavView.setOnItemSelectedListener { item ->
            navToPageIndex[item.itemId]?.let { pageIndex ->
                toggleContent(useViewPager = true)
                viewPager.currentItem = pageIndex
                true
            } ?: false
        }
    }

    // Mengatur visibilitas antara ViewPager2 dan container fragment tunggal sesuai mode tampilan aktif.
    private fun toggleContent(useViewPager: Boolean) = with(binding) {
        viewPager.isVisible = useViewPager
        fragmentContainer.isVisible = !useViewPager
    }

    // Menampilkan fragment tertentu ke dalam container dan menyembunyikan ViewPager2.
    private fun replaceFragment(fragment: Fragment) {
        toggleContent(useViewPager = false)
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    // Menangani navigasi item drawer dengan mengarahkan ke tab ViewPager2 atau mengganti fragment.
    override fun onDrawerItemSelected(itemId: Int) {
        when (itemId) {
            R.id.drawer_owner_dashboard, R.id.nav_home, R.id.nav_profile -> selectBottomNav(R.id.dashboard)
            R.id.drawer_owner_users -> selectBottomNav(R.id.cashier)
            R.id.drawer_owner_logs, R.id.nav_log_device -> replaceFragment(DeviceLogFragment())
            R.id.nav_booking_history -> replaceFragment(BookingHistoryFragment())
            R.id.nav_fnb_catalog -> replaceFragment(OwnerFnbFragment())
        }
    }

    // Memilih item bottom navigation tertentu dan memastikan ViewPager2 tampil di layar.
    private fun selectBottomNav(menuItemId: Int) {
        toggleContent(useViewPager = true)
        binding.bottomNavView.selectedItemId = menuItemId
    }
}