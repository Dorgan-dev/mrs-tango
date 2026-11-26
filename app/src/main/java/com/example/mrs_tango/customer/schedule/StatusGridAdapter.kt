package com.example.mrs_tango.customer.schedule

import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.mrs_tango.R
import com.example.mrs_tango.databinding.CustomerItemScheduleStatusBinding

class StatusGridAdapter(private val statusRows: List<List<String>>) :
    RecyclerView.Adapter<StatusGridAdapter.StatusViewHolder>() {

    inner class StatusViewHolder(val binding: CustomerItemScheduleStatusBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val binding = CustomerItemScheduleStatusBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StatusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val statuses = statusRows[position]
        val context = holder.itemView.context
        val bgColor = if (position % 2 == 0) Color.WHITE else ContextCompat.getColor(context, R.color.light_gray)

        holder.binding.llUnitStatuses.setBackgroundColor(bgColor)
        holder.binding.llUnitStatuses.removeAllViews()

        val density = context.resources.displayMetrics.density
        val cellWidthPx = (95 * density).toInt()
        val customFont = ResourcesCompat.getFont(context, R.font.commissioner_regular)

        for (status in statuses) {
            val tvStatus = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(cellWidthPx, LinearLayout.LayoutParams.MATCH_PARENT)
                text = status
                gravity = Gravity.CENTER
                textSize = if (status == "-") 12f else 11f
                typeface = customFont
                setBackgroundResource(R.drawable.bg_table_cell)

                when {
                    status.equals("Digunakan", true) -> {
                        setTextColor(Color.parseColor("#D32F2F"))
                        setTypeface(customFont, Typeface.BOLD)
                    }
                    status.equals("Dibooking", true) -> {
                        setTextColor(Color.parseColor("#F57C00"))
                        setTypeface(customFont, Typeface.BOLD)
                    }
                    else -> setTextColor(Color.parseColor("#333333"))
                }
            }
            holder.binding.llUnitStatuses.addView(tvStatus)
        }
    }

    override fun getItemCount(): Int = statusRows.size
}