package com.example.mrs_tango.owner.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.mrs_tango.R
import com.example.mrs_tango.core.base.BaseDrawerActivity
import com.example.mrs_tango.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        kode untuk mengaktifkan sidebar di ikon burger
        val baseActivity = requireActivity() as? BaseDrawerActivity
        baseActivity?.unlockDrawer()

        binding.iconMenu.setOnClickListener {
            baseActivity?.openDrawer()
        }
    }
}