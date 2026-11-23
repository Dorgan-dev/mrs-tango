package com.example.mrs_tango.owner

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mrs_tango.owner.cashier.CashierFragment
import com.example.mrs_tango.owner.dashboard.DashboardFragment
import com.example.mrs_tango.owner.device.DeviceFragment
import com.example.mrs_tango.owner.report.ReportFragment

class OwnerPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) { // Urutkan sesuai posisi item di menu/bottom_nav_owner.xml
            0 -> DashboardFragment()
            1 -> ReportFragment()
            2 -> DeviceFragment()
            3 -> CashierFragment()
            else -> DashboardFragment()
        }
    }
}