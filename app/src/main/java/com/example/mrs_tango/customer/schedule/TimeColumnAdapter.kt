package com.example.mrs_tango.customer.schedule

import android.R.attr.text
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mrs_tango.R
import com.example.mrs_tango.databinding.CustomerItemScheduleTimeBinding

class TimeColumnAdapter(private val times: List<String>) :
    RecyclerView.Adapter<TimeColumnAdapter.TimeViewHolder>() {

    inner class TimeViewHolder(val binding: CustomerItemScheduleTimeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val binding = CustomerItemScheduleTimeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val context = holder.itemView.context
        val bgColor = if (position % 2 == 0) Color.WHITE else ContextCompat.getColor(context, R.color.light_gray)

        holder.binding.tvTime.apply {
            text = times[position]
            setBackgroundColor(bgColor)
        }
    }

    override fun getItemCount(): Int = times.size
}