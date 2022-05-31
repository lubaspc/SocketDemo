package com.lubaspc.connectios

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
    private val messages = mutableListOf<String>()

    inner class ViewHolder(val tv: TextView) : RecyclerView.ViewHolder(tv) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(TextView(parent.context))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tv.text = messages[position]
        holder.tv.textSize = 18f
        (holder.tv.layoutParams as? RecyclerView.LayoutParams)?.apply {
            setMargins(12,8,12,0)
            width = LinearLayout.LayoutParams.MATCH_PARENT
        }
    }

    override fun getItemCount() = messages.size

    fun addMessage(txt: String) {
        messages.add(txt)
        notifyItemInserted(messages.size - 1)
    }
}