package com.example.mrs_tango.DummyData

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mrs_tango.R

class ConsoleAdapter(
    private val listConsole: ArrayList<Console>,
    private val onItemClick: (Console) -> Unit // Tambahkan callback ini
) : RecyclerView.Adapter<ConsoleAdapter.ListViewHolder>() {

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhoto: ImageView = itemView.findViewById(R.id.img_console)
        val tvName: TextView = itemView.findViewById(R.id.tv_console_name)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_console_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.console_card, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val console = listConsole[position]
        holder.imgPhoto.setImageResource(console.imagePhoto)
        holder.tvName.text = console.name
        holder.tvPrice.text = console.price

        // Panggil callback saat item diklik
        holder.itemView.setOnClickListener { onItemClick(console) }
    }

    override fun getItemCount(): Int = listConsole.size
}