package com.example.mrs_tango.customer.schedule

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mrs_tango.R
import com.example.mrs_tango.core.base.BaseDrawerActivity
import com.example.mrs_tango.customer.schedule.Tutorial.TutorialMessageActivity
import com.example.mrs_tango.databinding.FragmentScheduleBinding

class ScheduleFragment : Fragment() {
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Schdule"
        }
        setHasOptionsMenu(true)
        setupSynchronizedTables()

        //kode untuk mengaktifkan sidebar di ikon burger
        val baseActivity = requireActivity() as? BaseDrawerActivity
        baseActivity?.unlockDrawer()

        binding.iconMenu.setOnClickListener {
            baseActivity?.openDrawer()
        }
    }

    private fun setupSynchronizedTables() {
        val timeList = mutableListOf<String>()
        val statusMatrix = mutableListOf<List<String>>()

        // Generate dari jam 07:00 sampai 06:00 (24 Jam)
        for (i in 0 until 24) {
            val hour = (7 + i) % 24
            timeList.add(String.format("%02d:00", hour))

            val statuses = when (hour) {
                13 -> listOf("Digunakan", "-", "-", "-")
                14 -> listOf("Dibooking", "-", "Dibooking", "-")
                15 -> listOf("-", "Dibooking", "-", "Digunakan")
                16 -> listOf("Dibooking", "Dibooking", "Dibooking", "-")
                else -> listOf("-", "-", "-", "-")
            }
            statusMatrix.add(statuses)
        }

        binding.rvTimeColumn.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TimeColumnAdapter(timeList)
        }

        binding.rvStatusGrid.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = StatusGridAdapter(statusMatrix)
        }

        // SINKRONISASI SCROLL VERTIKAL ANTARA KIRI DAN KANAN
        val scrollListeners = arrayOfNulls<RecyclerView.OnScrollListener>(2)

        scrollListeners[0] = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                binding.rvStatusGrid.removeOnScrollListener(scrollListeners[1]!!)
                binding.rvStatusGrid.scrollBy(0, dy)
                binding.rvStatusGrid.addOnScrollListener(scrollListeners[1]!!)
            }
        }

        scrollListeners[1] = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                binding.rvTimeColumn.removeOnScrollListener(scrollListeners[0]!!)
                binding.rvTimeColumn.scrollBy(0, dy)
                binding.rvTimeColumn.addOnScrollListener(scrollListeners[0]!!)
            }
        }

        binding.rvTimeColumn.addOnScrollListener(scrollListeners[0]!!)
        binding.rvStatusGrid.addOnScrollListener(scrollListeners[1]!!)
    }
}