package com.example.mrs_tango.Schedule.Tutorial

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mrs_tango.R

class TutorialFragmentAdapter (
    activity: TutorialMessageActivity,
    private val fragments: List<Fragment>
)  : FragmentStateAdapter(activity)  {
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}