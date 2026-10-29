package com.example.mrs_tango

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ConsoleAdapter(private val listConsole: ArrayList<Console>) : RecyclerView.Adapter<ConsoleAdapter.ListViewHolder>() {

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Pastikan ID ini sama dengan yang ada di console_card.xml Anda
        val imgPhoto: ImageView = itemView.findViewById(R.id.img_console)
        val tvName: TextView = itemView.findViewById(R.id.tv_console_name)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_console_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.console_card, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val (id, name, price, imagePhoto) = listConsole[position]

        holder.imgPhoto.setImageResource(imagePhoto)
        holder.tvName.text = name
        holder.tvPrice.text = price

        holder.itemView.setOnClickListener {
            // Logika klik untuk pindah ke DetailFragment akan diletakkan di sini
        }
    }

    override fun getItemCount(): Int = listConsole.size
}